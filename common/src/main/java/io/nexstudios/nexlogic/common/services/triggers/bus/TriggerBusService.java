package io.nexstudios.nexlogic.common.services.triggers.bus;

import io.nexstudios.nexlogic.common.effects.model.CompiledAction;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.serviceregistry.di.Service;

import java.util.List;
import java.util.Map;

public interface TriggerBusService extends Service {
  void fire(String triggerId, LogicContext ctx);
  
  void updateActive(Map<String, List<CompiledAction>> newMap);
}