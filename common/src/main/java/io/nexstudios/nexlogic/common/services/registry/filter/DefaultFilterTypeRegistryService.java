package io.nexstudios.nexlogic.common.services.registry.filter;

import io.nexstudios.nexlogic.common.types.FilterTypeService;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultFilterTypeRegistryService implements FilterTypeRegistryService {

  private final Map<String, FilterTypeService> types = new ConcurrentHashMap<>();

  @Override
  public void register(FilterTypeService type) {
    Objects.requireNonNull(type, "type");
    types.put(type.id().toLowerCase(), type);
  }

  @Override
  public Optional<FilterTypeService> resolve(String id) {
    if (id == null) return Optional.empty();
    return Optional.ofNullable(types.get(id.toLowerCase()));
  }
}