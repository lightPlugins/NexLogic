package io.nexstudios.nexlogic.common.services.types;

import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.runtime.ConditionInstance;
import io.nexstudios.serviceregistry.di.Service;

public interface ConditionTypeService extends Service {
  String id();
  ConditionInstance create(ConfigSection config);
}