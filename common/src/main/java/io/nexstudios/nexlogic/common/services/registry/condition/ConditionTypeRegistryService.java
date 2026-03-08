package io.nexstudios.nexlogic.common.services.registry.condition;

import io.nexstudios.nexlogic.common.effects.types.ConditionTypeService;
import io.nexstudios.serviceregistry.di.Service;

import java.util.Optional;

public interface ConditionTypeRegistryService extends Service {
  void register(ConditionTypeService type);
  Optional<ConditionTypeService> resolve(String id);
}