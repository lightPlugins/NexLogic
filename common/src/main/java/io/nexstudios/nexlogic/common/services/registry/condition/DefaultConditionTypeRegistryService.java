package io.nexstudios.nexlogic.common.services.registry.condition;

import io.nexstudios.nexlogic.common.services.types.ConditionTypeService;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultConditionTypeRegistryService implements ConditionTypeRegistryService {
  private final Map<String, ConditionTypeService> types = new ConcurrentHashMap<>();

  @Override
  public void register(ConditionTypeService type) {
    Objects.requireNonNull(type, "type");
    types.put(type.id().toLowerCase(), type);
  }

  @Override
  public Optional<ConditionTypeService> resolve(String id) {
    if (id == null) return Optional.empty();
    return Optional.ofNullable(types.get(id.toLowerCase()));
  }
}