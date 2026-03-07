package io.nexstudios.nexlogic.common.services.triggers.bus;

import io.nexstudios.nexlogic.common.model.CompiledAction;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.serviceregistry.di.Service;

import java.util.List;
import java.util.Map;

public interface TriggerBusService extends Service {
  void fire(String triggerId, LogicContext ctx);
  void swap(Map<String, List<CompiledAction>> compiled);
}