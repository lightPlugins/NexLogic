package io.nexstudios.nexlogic.common.services.registry.addon;

import io.nexstudios.nexlogic.common.effects.addon.NexLogicAddon;
import io.nexstudios.nexlogic.common.services.registry.condition.ConditionTypeRegistryService;
import io.nexstudios.nexlogic.common.effects.types.ConditionTypeService;
import io.nexstudios.nexlogic.common.services.registry.effect.EffectTypeRegistryService;
import io.nexstudios.nexlogic.common.effects.types.EffectTypeService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.util.Objects;

@Dependencies({
    EffectTypeRegistryService.class,
    ConditionTypeRegistryService.class
})
public final class DefaultAddonRegistryService implements AddonRegistryService {

  private final ServiceAccessor service;
  private final EffectTypeRegistryService effects;
  private final ConditionTypeRegistryService conditions;

  public DefaultAddonRegistryService(ServiceAccessor service) {
    this.service = service;
    this.effects = service.getService(EffectTypeRegistryService.class);
    this.conditions = service.getService(ConditionTypeRegistryService.class);
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
        registerEffectType(service.create(effectTypeClass));
      }

      @Override
      public void registerConditionType(Class<? extends ConditionTypeService> conditionTypeClass) {
        registerConditionType(service.create(conditionTypeClass));
      }
    });
  }
}