package io.nexstudios.nexlogic.bukkit.services.mvp;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.types.conditions.PermissionConditionType;
import io.nexstudios.nexlogic.common.types.effects.MessageEffectType;
import io.nexstudios.nexlogic.common.addon.NexLogicAddon;
import io.nexstudios.nexlogic.common.services.registry.addon.AddonRegistryService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.Service;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

@Dependencies({
    AddonRegistryService.class
})
public final class BukkitMvpTypesService implements Service {

  private final ServiceAccessor services;
  private final AddonRegistryService addons;

  public BukkitMvpTypesService(PaperPluginService core) {
    this.services = core.plugin().services();
    this.addons = services.getService(AddonRegistryService.class);
  }

  public void register() {
    addons.registerAddon(new NexLogicAddon() {
      @Override
      public void register(Registration r) {
        r.registerConditionType(PermissionConditionType.class);
        r.registerEffectType(MessageEffectType.class);
      }
    });
  }
}