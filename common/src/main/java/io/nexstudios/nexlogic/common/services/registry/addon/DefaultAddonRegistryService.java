package io.nexstudios.nexlogic.common.services.registry.addon;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.addon.NexLogicAddon;
import io.nexstudios.nexlogic.common.services.registry.condition.ConditionTypeRegistryService;
import io.nexstudios.nexlogic.common.services.types.ConditionTypeService;
import io.nexstudios.nexlogic.common.services.registry.effect.EffectTypeRegistryService;
import io.nexstudios.nexlogic.common.services.types.effect.EffectTypeService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.util.Objects;

@Dependencies({
    EffectTypeRegistryService.class,
    ConditionTypeRegistryService.class
})
public final class DefaultAddonRegistryService implements AddonRegistryService {

  private final ServiceAccessor services;
  private final EffectTypeRegistryService effects;
  private final ConditionTypeRegistryService conditions;

  public DefaultAddonRegistryService(PaperPluginService core) {
    this.services = core.plugin().services();
    this.effects = services.getService(EffectTypeRegistryService.class);
    this.conditions = services.getService(ConditionTypeRegistryService.class);
  }

  @Override
  public void registerAddon(NexLogicAddon addon) {
    Objects.requireNonNull(addon, "addon");
    addon.register(new NexLogicAddon.Registration() {
      @Override
      public void registerEffectType(EffectTypeService effectType) {
        effects.register(effectType);
      }

      @Override
      public void registerConditionType(ConditionTypeService conditionType) {
        conditions.register(conditionType);
      }

      @Override
      public void registerEffectType(Class<? extends EffectTypeService> effectTypeClass) {
        registerEffectType(services.create(effectTypeClass));
      }

      @Override
      public void registerConditionType(Class<? extends ConditionTypeService> conditionTypeClass) {
        registerConditionType(services.create(conditionTypeClass));
      }
    });
  }
}