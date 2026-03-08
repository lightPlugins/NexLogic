package io.nexstudios.nexlogic.common.effects.types;

import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.serviceregistry.di.Service;

import java.util.Set;
import java.util.function.Predicate;

public interface FilterTypeService extends Service {
  String id();

  Set<ContextCapability> requiredCapabilities();

  Predicate<LogicContext> compile(String triggerId, ConfigSection args);
}