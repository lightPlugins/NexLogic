package io.nexstudios.nexlogic.bukkit.services.effects.bootstrap;

import io.nexstudios.nexlogic.bukkit.effects.conditions.ChanceConditionType;
import io.nexstudios.nexlogic.bukkit.effects.conditions.HasAgeConditionType;
import io.nexstudios.nexlogic.bukkit.effects.conditions.PermissionConditionType;
import io.nexstudios.nexlogic.bukkit.effects.conditions.PlayerPlacedConditionType;
import io.nexstudios.nexlogic.bukkit.effects.effects.GiveItemEffectType;
import io.nexstudios.nexlogic.bukkit.effects.effects.GiveHeadEffectType;
import io.nexstudios.nexlogic.bukkit.effects.effects.LogEffectType;
import io.nexstudios.nexlogic.bukkit.effects.effects.MessageEffectType;
import io.nexstudios.nexlogic.bukkit.effects.effects.PlaySoundEffectType;
import io.nexstudios.nexlogic.bukkit.effects.filters.BlocksFilterType;
import io.nexstudios.nexlogic.bukkit.effects.filters.EntityFilterType;
import io.nexstudios.nexlogic.bukkit.effects.filters.WorldsFilterType;
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
      r.registerConditionType(PlayerPlacedConditionType.class);
      r.registerConditionType(HasAgeConditionType.class);
      r.registerEffectType(MessageEffectType.class);
      r.registerEffectType(LogEffectType.class);
      r.registerEffectType(GiveItemEffectType.class);
      r.registerEffectType(GiveHeadEffectType.class);
      r.registerEffectType(PlaySoundEffectType.class);
    });
  }

  private void registerFilterTypes() {
    filters.register(service.create(BlocksFilterType.class));
    filters.register(service.create(WorldsFilterType.class));
    filters.register(service.create(EntityFilterType.class));
  }
}