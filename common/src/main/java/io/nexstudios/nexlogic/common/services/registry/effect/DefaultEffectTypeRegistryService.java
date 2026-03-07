package io.nexstudios.nexlogic.common.services.registry.effect;

import io.nexstudios.nexlogic.common.types.EffectTypeService;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultEffectTypeRegistryService implements EffectTypeRegistryService {
  private final Map<String, EffectTypeService> types = new ConcurrentHashMap<>();

  @Override
  public void register(EffectTypeService type) {
    Objects.requireNonNull(type, "type");
    types.put(type.id().toLowerCase(), type);
  }

  @Override
  public Optional<EffectTypeService> resolve(String id) {
    if (id == null) return Optional.empty();
    return Optional.ofNullable(types.get(id.toLowerCase()));
  }
}