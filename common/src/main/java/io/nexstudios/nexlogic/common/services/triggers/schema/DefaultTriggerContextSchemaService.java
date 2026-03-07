package io.nexstudios.nexlogic.common.services.triggers.schema;

import java.util.Map;
import java.util.Set;

public final class DefaultTriggerContextSchemaService implements TriggerContextSchemaService {

  private final Map<String, Set<ContextCapability>> schema = Map.of(
      "join", Set.of(ContextCapability.PLAYER, ContextCapability.WORLD, ContextCapability.LOCATION),
      "break_block", Set.of(ContextCapability.PLAYER, ContextCapability.WORLD, ContextCapability.LOCATION, ContextCapability.BLOCK)
  );

  @Override
  public Set<ContextCapability> capabilities(String triggerId) {
    if (triggerId == null) return Set.of();
    return schema.getOrDefault(triggerId.toLowerCase(), Set.of());
  }
}