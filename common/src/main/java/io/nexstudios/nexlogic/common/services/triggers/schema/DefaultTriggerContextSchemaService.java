package io.nexstudios.nexlogic.common.services.triggers.schema;

import java.util.Map;
import java.util.Set;

public final class DefaultTriggerContextSchemaService implements TriggerContextSchemaService {

  private final Map<String, Set<ContextCapability>> schema = Map.of(
      "player-join", Set.of(ContextCapability.PLAYER, ContextCapability.WORLD, ContextCapability.LOCATION),
      "block-break", Set.of(ContextCapability.PLAYER, ContextCapability.WORLD, ContextCapability.LOCATION, ContextCapability.BLOCK),
      "block-place", Set.of(ContextCapability.PLAYER, ContextCapability.WORLD, ContextCapability.LOCATION, ContextCapability.BLOCK),
      "entity-death", Set.of(ContextCapability.PLAYER, ContextCapability.WORLD, ContextCapability.LOCATION, ContextCapability.ENTITY)
  );

  @Override
  public Set<ContextCapability> capabilities(String triggerId) {
    if (triggerId == null) return Set.of();
    return schema.getOrDefault(triggerId.toLowerCase(), Set.of());
  }
}