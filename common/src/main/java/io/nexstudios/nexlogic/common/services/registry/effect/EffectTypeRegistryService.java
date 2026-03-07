package io.nexstudios.nexlogic.common.services.registry.effect;

import io.nexstudios.nexlogic.common.types.EffectTypeService;
import io.nexstudios.serviceregistry.di.Service;

import java.util.Optional;

public interface EffectTypeRegistryService extends Service {
  void register(EffectTypeService type);
  Optional<EffectTypeService> resolve(String id);
}