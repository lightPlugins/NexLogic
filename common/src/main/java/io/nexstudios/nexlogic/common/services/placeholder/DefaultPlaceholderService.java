package io.nexstudios.nexlogic.common.services.placeholder;

import io.nexstudios.nexlogic.common.placeholder.PlaceholderKey;
import io.nexstudios.nexlogic.common.placeholder.PlaceholderProvider;
import io.nexstudios.nexlogic.common.placeholder.PlaceholderResolveContext;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.nexlogic.common.services.placeholder.cache.PlaceholderCacheOptionsService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Dependencies({
    LoggerService.class,
    PlaceholderCacheOptionsService.class
})
public final class DefaultPlaceholderService implements PlaceholderService {

  private static final Pattern TOKEN = Pattern.compile("%([^%]+)%");

  /**
   * Warn at most once per cooldown window when cache overflows.
   */
  private static final long OVERFLOW_WARN_COOLDOWN_NANOS = Duration.ofSeconds(30).toNanos();
  private static final long EXPIRED_CLEANUP_EVERY_PUTS = 1024L;

  private final LoggerService logger;
  private final PlaceholderCacheOptionsService cacheOptions;
  private final AtomicLong lastOverflowWarnNanos = new AtomicLong(0L);
  private final AtomicLong cachePutCounter = new AtomicLong(0L);

  private final ConcurrentHashMap<String, Set<PlaceholderKey>> keysByOwner = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<PlaceholderKey, Entry> registry = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<CacheKey, CacheEntry> cache = new ConcurrentHashMap<>();

  public DefaultPlaceholderService(ServiceAccessor services) {
    this.logger = services.getService(LoggerService.class);
    this.cacheOptions = services.getService(PlaceholderCacheOptionsService.class);
  }

  @Override
  public void register(String owner, PlaceholderKey key, PlaceholderProvider provider, Duration ttl) {
    if (owner == null || owner.isBlank()) throw new IllegalArgumentException("owner is required");
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(provider, "provider");
    ttl = ttl == null ? Duration.ZERO : ttl;

    final String normalizedOwner = owner.trim().toLowerCase();

    Entry newEntry = new Entry(normalizedOwner, provider, ttl);

    Entry previous = registry.put(key, newEntry);

    // If the key was previously registered under a different owner, fix indices and cached values.
    if (previous != null && !normalizedOwner.equals(previous.owner)) {
      Set<PlaceholderKey> oldSet = keysByOwner.get(previous.owner);
      if (oldSet != null) {
        oldSet.remove(key);
        if (oldSet.isEmpty()) {
          keysByOwner.remove(previous.owner, oldSet);
        }
      }

      // Drop cached values tied to the old owner for this key (avoid stale retention)
      cache.keySet().removeIf(k -> k.owner.equals(previous.owner) && k.key.equals(key));
    }

    keysByOwner.compute(normalizedOwner, (o, set) -> {
      Set<PlaceholderKey> out = (set == null) ? ConcurrentHashMap.newKeySet() : set;
      out.add(key);
      return out;
    });
  }

  @Override
  public void unregisterOwner(String owner) {
    if (owner == null || owner.isBlank()) return;
    String o = owner.trim().toLowerCase();

    Set<PlaceholderKey> keys = keysByOwner.remove(o);
    if (keys == null || keys.isEmpty()) return;

    for (PlaceholderKey k : keys) {
      Entry e = registry.get(k);
      if (e != null && o.equals(e.owner)) {
        registry.remove(k, e);
      }
    }

    cache.keySet().removeIf(k -> k.owner.equals(o));
  }

  @Override
  public Optional<String> resolveSingle(String token, PlaceholderResolveContext ctx, ResolveOptions options) {
    if (token == null) return Optional.empty();
    String raw = token.trim();
    if (raw.startsWith("%") && raw.endsWith("%") && raw.length() >= 3) {
      raw = raw.substring(1, raw.length() - 1);
    }

    Parse p = parse(raw, ctx == null ? "" : ctx.defaultIdentifier());
    if (p == null) return Optional.empty();

    return Optional.ofNullable(resolveKey(p.key, ctx, options, new ArrayDeque<>(), 0));
  }

  @Override
  public String resolveText(String input, PlaceholderResolveContext ctx, ResolveOptions options) {
    if (input == null || input.isEmpty()) return input == null ? "" : input;
    Objects.requireNonNull(options, "options");

    Matcher m = TOKEN.matcher(input);
    StringBuilder out = new StringBuilder(input.length());

    int last = 0;
    int tokens = 0;

    while (m.find()) {
      tokens++;
      if (tokens > options.maxTokensPerInput()) {
        logger.logger().warning("Placeholder resolve aborted: too many tokens in one input (max=" + options.maxTokensPerInput() + ")");
        break;
      }

      out.append(input, last, m.start());
      String inside = m.group(1);

      Parse p = parse(inside, ctx == null ? "" : ctx.defaultIdentifier());
      if (p == null) {
        out.append(m.group(0));
      } else {
        String resolved = resolveKey(p.key, ctx, options, new ArrayDeque<>(), 0);
        out.append(resolved == null ? m.group(0) : resolved);
      }

      last = m.end();
    }

    out.append(input, last, input.length());
    return out.toString();
  }

  private String resolveKey(
      PlaceholderKey key,
      PlaceholderResolveContext ctx,
      ResolveOptions options,
      Deque<PlaceholderKey> stack,
      int depth
  ) {
    if (depth >= options.maxDepth()) {
      logger.logger().warning("Placeholder resolve aborted: maxDepth reached (" + options.maxDepth() + ") at " + key);
      return null;
    }

    if (stack.contains(key)) {
      logger.logger().warning("Placeholder resolve aborted: cycle detected " + cycleString(stack, key));
      return null;
    }

    Entry entry = registry.get(key);
    if (entry == null) return null;

    CacheKey ck = new CacheKey(entry.owner, key, ctx == null ? "global" : ctx.cacheScopeKey());
    String cached = getCached(ck);
    if (cached != null) return cached;

    stack.addLast(key);
    try {
      String value = entry.provider.resolve(ctx == null ? PlaceholderResolveContext.of(null, key.identifier()) : ctx);

      if (value == null) return null;

      String expanded = expandNested(value, ctx, options, stack, depth + 1);

      putCached(ck, expanded, entry.ttl);
      return expanded;
    } catch (Throwable t) {
      logger.logger().severe("Placeholder '" + key + "' threw while resolving: " + t.getMessage());
      return null;
    } finally {
      stack.removeLast();
    }
  }

  private String expandNested(
      String value,
      PlaceholderResolveContext ctx,
      ResolveOptions options,
      Deque<PlaceholderKey> stack,
      int depth
  ) {
    if (value == null || value.isEmpty()) return value;

    Matcher m = TOKEN.matcher(value);
    if (!m.find()) return value;

    StringBuilder out = new StringBuilder(value.length());
    int last = 0;

    m.reset();
    while (m.find()) {
      out.append(value, last, m.start());
      Parse p = parse(m.group(1), ctx == null ? "" : ctx.defaultIdentifier());
      if (p == null) {
        out.append(m.group(0));
      } else {
        String resolved = resolveKey(p.key, ctx, options, stack, depth);
        out.append(resolved == null ? m.group(0) : resolved);
      }
      last = m.end();
    }
    out.append(value, last, value.length());
    return out.toString();
  }

  private static String cycleString(Deque<PlaceholderKey> stack, PlaceholderKey key) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (PlaceholderKey k : stack) sb.append(k).append(" -> ");
    sb.append(key).append("]");
    return sb.toString();
  }

  private static Parse parse(String inside, String defaultIdentifier) {
    if (inside == null) return null;
    String s = inside.trim();
    if (s.isBlank()) return null;

    String identifier;
    String id;

    int idx = s.indexOf(':');
    if (idx >= 0) {
      identifier = s.substring(0, idx).trim();
      id = s.substring(idx + 1).trim();
    } else {
      identifier = defaultIdentifier == null ? "" : defaultIdentifier.trim();
      id = s;
    }

    if (identifier.isBlank()) return null;
    if (id.isBlank()) return null;

    return new Parse(PlaceholderKey.of(identifier, id));
  }

  private String getCached(CacheKey key) {
    CacheEntry e = cache.get(key);
    if (e == null) return null;

    long now = System.nanoTime();
    if (e.expiresAtNanos != 0L && now >= e.expiresAtNanos) {
      cache.remove(key, e);
      return null;
    }
    return e.value;
  }

  private void putCached(CacheKey key, String value, Duration ttl) {
    if (ttl == null || ttl.isZero() || ttl.isNegative()) return;

    long expires = System.nanoTime() + ttl.toNanos();
    cache.put(key, new CacheEntry(value, expires));

    // Opportunistic cleanup: keeps expired entries from lingering when cache never overflows.
    long n = cachePutCounter.incrementAndGet();
    if ((n & (EXPIRED_CLEANUP_EVERY_PUTS - 1)) == 0L) {
      cleanupExpiredEntries();
    }

    enforceCacheLimitIfNeeded();
  }

  private void enforceCacheLimitIfNeeded() {
    int max = Math.max(1, cacheOptions.maxCacheEntries());

    int size = cache.size();
    if (size <= max) return;

    // First remove expired entries
    cleanupExpiredEntries();

    int afterCleanup = cache.size();
    if (afterCleanup <= max) return;

    int target = (int) (max * 0.95); // small hysteresis
    int toRemove = Math.max(1, afterCleanup - target);

    int removed = 0;
    Iterator<CacheKey> it = cache.keySet().iterator();
    while (it.hasNext() && removed < toRemove) {
      it.next();
      it.remove();
      removed++;
    }

    warnOverflow(afterCleanup, removed, max);
  }

  private void cleanupExpiredEntries() {
    long now = System.nanoTime();
    for (var e : cache.entrySet()) {
      CacheEntry ce = e.getValue();
      if (ce == null) continue;

      long expires = ce.expiresAtNanos;
      if (expires != 0L && now >= expires) {
        cache.remove(e.getKey(), ce);
      }
    }
  }

  private void warnOverflow(int sizeBeforeEvict, int removed, int max) {
    long now = System.nanoTime();
    long last = lastOverflowWarnNanos.get();

    if (now - last < OVERFLOW_WARN_COOLDOWN_NANOS) return;
    if (!lastOverflowWarnNanos.compareAndSet(last, now)) return;

    logger.logger().warning(
        "Placeholder cache overflow protection triggered: size=" + sizeBeforeEvict +
            ", max=" + max +
            ", evicted=" + removed +
            ". Consider reducing TTL, limiting cache scopes, or lowering placeholder usage."
    );
  }

  private record Entry(String owner, PlaceholderProvider provider, Duration ttl) {}
  private record Parse(PlaceholderKey key) {}
  private record CacheKey(String owner, PlaceholderKey key, String scope) {}
  private record CacheEntry(String value, long expiresAtNanos) {}

}