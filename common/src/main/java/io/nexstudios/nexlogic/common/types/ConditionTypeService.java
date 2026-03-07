package io.nexstudios.nexlogic.common.types;

import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.serviceregistry.di.Service;

import java.util.Set;

public interface ConditionTypeService extends Service {
  String id();

  default Set<ContextCapability> requiredCapabilities() {
    return Set.of();
  }

  ConditionInstance create(ConfigSection config);
}