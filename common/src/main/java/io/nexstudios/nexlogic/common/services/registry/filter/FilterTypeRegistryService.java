package io.nexstudios.nexlogic.common.services.registry.filter;

import io.nexstudios.nexlogic.common.types.FilterTypeService;
import io.nexstudios.serviceregistry.di.Service;
import java.util.Optional;

public interface FilterTypeRegistryService extends Service {
  void register(FilterTypeService type);
  Optional<FilterTypeService> resolve(String id);
}
