package io.nexstudios.nexlogic.common.services.types.mvp;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.addon.NexLogicAddon;
import io.nexstudios.nexlogic.common.services.registry.addon.AddonRegistryService;
import io.nexstudios.nexlogic.common.types.conditions.ChanceConditionType;
import io.nexstudios.nexlogic.common.types.effects.LogEffectType;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.Service;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

@Dependencies({
    AddonRegistryService.class
})
public final class CommonMvpTypesService implements Service {

  private final ServiceAccessor services;
  private final AddonRegistryService addons;

  public CommonMvpTypesService(PaperPluginService core) {
    this.services = core.plugin().services();
    this.addons = services.getService(AddonRegistryService.class);
  }

  public void register() {
    addons.registerAddon(new NexLogicAddon() {
      @Override
      public void register(Registration r) {
        registerConditions(r);
        registerEffects(r);
      }
    });
  }

  private void registerConditions(NexLogicAddon.Registration registration) {
    registration.registerConditionType(ChanceConditionType.class);
  }

  private void registerEffects(NexLogicAddon.Registration registration) {
    registration.registerEffectType(LogEffectType.class);
  }
}