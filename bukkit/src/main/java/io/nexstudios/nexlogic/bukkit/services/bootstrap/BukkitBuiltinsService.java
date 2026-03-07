package io.nexstudios.nexlogic.bukkit.services.bootstrap;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
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
public final class BukkitBuiltinsService implements Service {

  private final ServiceAccessor services;
  private final AddonRegistryService addons;
  private final FilterTypeRegistryService filters;

  public BukkitBuiltinsService(PaperPluginService core) {
    this.services = core.plugin().services();
    this.addons = services.getService(AddonRegistryService.class);
    this.filters = services.getService(FilterTypeRegistryService.class);
  }

  public void registerAll() {
    registerMvpTypes();
    registerFilterTypes();
  }

  private void registerMvpTypes() {
    addons.registerAddon(r -> {
      r.registerConditionType(ChanceConditionType.class);
      r.registerConditionType(PermissionConditionType.class);
      r.registerEffectType(MessageEffectType.class);
      r.registerEffectType(LogEffectType.class);
    });
  }

  private void registerFilterTypes() {
    filters.register(services.create(BlocksFilterType.class));
    filters.register(services.create(WorldsFilterType.class));
  }
}