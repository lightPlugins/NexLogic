package io.nexstudios.nexlogic.common.addon;

import io.nexstudios.nexlogic.common.types.ConditionTypeService;
import io.nexstudios.nexlogic.common.types.EffectTypeService;

public interface NexLogicAddon {
  void register(Registration registration);

  interface Registration {
    void registerEffectType(EffectTypeService effectType);
    void registerConditionType(ConditionTypeService conditionType);

    void registerEffectType(Class<? extends EffectTypeService> effectTypeClass);
    void registerConditionType(Class<? extends ConditionTypeService> conditionTypeClass);
  }
}