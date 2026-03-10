package io.nexstudios.nexlogic.bukkit.effects.filters;

import io.nexstudios.nexlogic.bukkit.services.effects.blocks.BlockKeyService;
import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextResolverService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.PlaceholderRuntimeService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.config.LogicData;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.effects.types.FilterTypeService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.nexlogic.common.effects.config.MapConfigSection;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Dependencies({
    BukkitContextResolverService.class,
    BlockKeyService.class,
    PlaceholderRuntimeService.class
})
public final class BlocksFilterType implements FilterTypeService {

  private final BukkitContextResolverService resolver;
  private final BlockKeyService blockKeys;
  private final PlaceholderRuntimeService placeholders;

  public BlocksFilterType(ServiceAccessor service) {
    this.resolver = service.getService(BukkitContextResolverService.class);
    this.blockKeys = service.getService(BlockKeyService.class);
    this.placeholders = service.getService(PlaceholderRuntimeService.class);
  }

  @Override
  public String id() {
    return "blocks";
  }

  @Override
  public Set<ContextCapability> requiredCapabilities() {
    return Set.of(ContextCapability.BLOCK);
  }

  @Override
  public Predicate<LogicContext> compile(String triggerId, ConfigSection args) {
    final ConfigSection baseArgs = (args == null) ? MapConfigSection.EMPTY : args;

    // Fast-path: if args contain no placeholder tokens, precompute everything once.
    if (!containsPlaceholderTokens(baseArgs.getValues(true))) {
      LogicData data = new LogicData(baseArgs);
      final boolean inverted = data.getBoolean("inverted", false);

      Set<String> blocksRaw = data.getStringSet("blocks");
      if (blocksRaw.isEmpty()) return ctx -> true;

      final Set<String> allowed = normalizeAllowed(blocksRaw);

      return ctx -> {
        Block block = resolver.block(ctx).orElse(null);
        if (block == null) return false;

        boolean match = blockKeys.matches(block, allowed);
        return inverted != match;
      };
    }

    // Fallback: dynamic (placeholders) -> resolve per invocation
    return ctx -> {
      ConfigSection resolvedArgs = placeholders.resolveSection(baseArgs, ctx);
      LogicData data = new LogicData(resolvedArgs);

      Set<String> blocksRaw = data.getStringSet("blocks");
      boolean inverted = data.getBoolean("inverted", false);

      if (blocksRaw.isEmpty()) return true;

      Set<String> allowed = normalizeAllowed(blocksRaw);

      Block block = resolver.block(ctx).orElse(null);
      if (block == null) return false;

      boolean match = blockKeys.matches(block, allowed);
      return inverted != match;
    };
  }

  private Set<String> normalizeAllowed(Set<String> blocksRaw) {
    HashSet<String> out = new HashSet<>(Math.max(16, blocksRaw.size() * 2));
    for (String s : blocksRaw) {
      String n = blockKeys.normalize(s);
      if (n != null && !n.isBlank()) out.add(n);
    }
    return out.isEmpty() ? Set.of() : Set.copyOf(out);
  }

  private static boolean containsPlaceholderTokens(Object v) {
    if (v == null) return false;

    if (v instanceof String s) {
      // simple heuristic: placeholder tokens always contain '%'
      return s.indexOf('%') >= 0;
    }

    if (v instanceof Map<?, ?> m) {
      for (Object e : m.entrySet()) {
        Map.Entry<?, ?> me = (Map.Entry<?, ?>) e;
        if (containsPlaceholderTokens(me.getKey())) return true;
        if (containsPlaceholderTokens(me.getValue())) return true;
      }
      return false;
    }

    if (v instanceof List<?> list) {
      for (Object o : list) if (containsPlaceholderTokens(o)) return true;
      return false;
    }

    return false;
  }
}