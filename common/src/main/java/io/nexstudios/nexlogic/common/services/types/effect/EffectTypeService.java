package io.nexstudios.nexlogic.common.services.types.effect;

import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.runtime.EffectInstance;
import io.nexstudios.serviceregistry.di.Service;

public interface EffectTypeService extends Service {
  String id();
  EffectInstance create(ConfigSection config);
}