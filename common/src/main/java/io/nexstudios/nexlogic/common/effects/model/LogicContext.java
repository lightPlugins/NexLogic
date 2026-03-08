package io.nexstudios.nexlogic.common.effects.model;

import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One LogicContext per trigger invocation.
 * This context is intentionally Bukkit-free and uses typed domains.
 *
 * extras() is kept as an escape hatch for custom integrations, but filters/conditions/effects
 * should prefer typed domains.
 */
public final class LogicContext {
  private final String triggerId;

  private final PlayerContext player;
  private final WorldContext world;
  private final LocationContext location;
  private final BlockContext block;
  private final EntityContext entity;
  private final ItemContext item;

  private final EnumSet<ContextCapability> capabilities;

  private final Map<String, Object> extras = new ConcurrentHashMap<>();

  // template constructor
  public LogicContext(String triggerId) {
    this(
        triggerId,
        null,
        null,
        null,
        null,
        null,
        null);
  }

  // template constructor with context
  public LogicContext(String triggerId, PlayerContext player) {
    this(
        triggerId,
        player,
        null,
        null,
        null,
        null,
        null);
  }

  public LogicContext(
      String triggerId,
      PlayerContext player,
      WorldContext world,
      LocationContext location,
      BlockContext block,
      EntityContext entity,
      ItemContext item
  ) {
    this.triggerId = Objects.requireNonNull(triggerId, "triggerId");

    this.player = player;
    this.world = world;
    this.location = location;
    this.block = block;
    this.entity = entity;
    this.item = item;

    this.capabilities = EnumSet.noneOf(ContextCapability.class);
    if (player != null) capabilities.add(ContextCapability.PLAYER);
    if (world != null) capabilities.add(ContextCapability.WORLD);
    if (location != null) capabilities.add(ContextCapability.LOCATION);
    if (block != null) capabilities.add(ContextCapability.BLOCK);
    if (entity != null) capabilities.add(ContextCapability.ENTITY);
    if (item != null) capabilities.add(ContextCapability.ITEM);
  }

  public String triggerId() {
    return triggerId;
  }

  public Set<ContextCapability> capabilities() {
    return Set.copyOf(capabilities);
  }
  public Optional<PlayerContext> player() {
    return Optional.ofNullable(player);
  }
  public Optional<WorldContext> world() {
    return Optional.ofNullable(world);
  }
  public Optional<LocationContext> location() {
    return Optional.ofNullable(location);
  }
  public Optional<BlockContext> block() {
    return Optional.ofNullable(block);
  }
  public Optional<EntityContext> entity() {
    return Optional.ofNullable(entity);
  }
  public Optional<ItemContext> item() {
    return Optional.ofNullable(item);
  }
  public Map<String, Object> extras() {
    return extras;
  }

  public <T> void put(ContextKey<T> key, T value) {
    putInternal(key, value);
  }

  public record Entry<T>(ContextKey<T> key, T value) {
    public Entry {
      Objects.requireNonNull(key, "key");
      // value can be null => means "remove", analog to put()
    }
  }

  public static <T> Entry<T> entry(ContextKey<T> key, T value) {
    return new Entry<>(key, value);
  }

  public void putAll(Entry<?>... entries) {
    if (entries == null) return;
    for (Entry<?> e : entries) {
      if (e == null) continue;
      putInternal(e.key(), e.value());
    }
  }

  private void putInternal(ContextKey<?> key, Object value) {
    Objects.requireNonNull(key, "key");
    if (value == null) {
      extras.remove(key.id());
      return;
    }
    if (!key.type().isInstance(value)) {
      throw new IllegalArgumentException(
          "Invalid value type for " + key + ": got " + value.getClass().getName()
      );
    }
    extras.put(key.id(), value);
  }

  public <T> Optional<T> get(ContextKey<T> key) {
    Objects.requireNonNull(key, "key");
    Object v = extras.get(key.id());
    if (v == null) return Optional.empty();
    if (!key.type().isInstance(v)) return Optional.empty();
    return Optional.of(key.type().cast(v));
  }

  // --- Typed domain objects ---

  public record PlayerContext(UUID uuid) {
    public PlayerContext {
      Objects.requireNonNull(uuid, "uuid");
    }
  }

  public record WorldContext(String name) {
    public WorldContext {
      name = Objects.requireNonNull(name, "name");
    }
  }

  public record LocationContext(String worldName, double x, double y, double z) {
    public LocationContext {
      worldName = Objects.requireNonNull(worldName, "worldName");
    }
  }

  public record BlockContext(String typeLower) {
    public BlockContext {
      typeLower = Objects.requireNonNull(typeLower, "typeLower").toLowerCase();
    }
  }

  public record EntityContext(String typeIdLower) {
    public EntityContext {
      typeIdLower = Objects.requireNonNull(typeIdLower, "typeIdLower").toLowerCase();
    }
  }

  public record ItemContext(String typeIdLower) {
    public ItemContext {
      typeIdLower = Objects.requireNonNull(typeIdLower, "typeIdLower").toLowerCase();
    }
  }
}