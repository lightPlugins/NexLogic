package io.nexstudios.nexlogic.common.services.filters;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.services.registry.filter.FilterTypeRegistryService;
import io.nexstudios.nexlogic.common.types.filters.BlocksFilterType;
import io.nexstudios.nexlogic.common.types.filters.WorldsFilterType;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.Service;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

@Dependencies({
    FilterTypeRegistryService.class
})
public final class CommonFilterTypesService implements Service {

  private final ServiceAccessor services;
  private final FilterTypeRegistryService registry;

  public CommonFilterTypesService(PaperPluginService core) {
    this.services = core.plugin().services();
    this.registry = services.getService(FilterTypeRegistryService.class);
  }

  public void register() {
    registry.register(services.create(BlocksFilterType.class));
    registry.register(services.create(WorldsFilterType.class));
  }
}