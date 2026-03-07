package io.nexstudios.nexlogic.bukkit.services.bootstrap;

import io.nexstudios.nexlogic.bukkit.types.conditions.ChanceConditionType;
import io.nexstudios.nexlogic.bukkit.types.conditions.PermissionConditionType;
import io.nexstudios.nexlogic.bukkit.types.effects.LogEffectType;
import io.nexstudios.nexlogic.bukkit.types.effects.MessageEffectType;
import io.nexstudios.nexlogic.bukkit.types.filters.BlocksFilterType;
import io.nexstudios.nexlogic.bukkit.types.filters.WorldsFilterType;
import io.nexstudios.nexlogic.common.services.registry.addon.AddonRegistryService;
import io.nexstudios.nexlogic.common.services.registry.filter.FilterTypeRegistryService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.Service;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

@Dependencies({
    AddonRegistryService.class,
    FilterTypeRegistryService.class
})
public final class TypeBuiltinService implements Service {

  private final ServiceAccessor service;
  private final AddonRegistryService addons;
  private final FilterTypeRegistryService filters;

  public TypeBuiltinService(ServiceAccessor service) {
    this.service = service;
    this.addons = service.getService(AddonRegistryService.class);
    this.filters = service.getService(FilterTypeRegistryService.class);
  }

  public void registerAll() {
    registerConditionAndEffectTypes();
    registerFilterTypes();
  }

  private void registerConditionAndEffectTypes() {
    addons.registerAddon(r -> {
      r.registerConditionType(ChanceConditionType.class);
      r.registerConditionType(PermissionConditionType.class);
      r.registerEffectType(MessageEffectType.class);
      r.registerEffectType(LogEffectType.class);
    });
  }

  private void registerFilterTypes() {
    filters.register(service.create(BlocksFilterType.class));
    filters.register(service.create(WorldsFilterType.class));
  }
}