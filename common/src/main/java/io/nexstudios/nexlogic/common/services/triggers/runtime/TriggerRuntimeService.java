package io.nexstudios.nexlogic.common.services.triggers.runtime;

import io.nexstudios.nexlogic.common.model.CompiledAction;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.serviceregistry.di.Service;

import java.util.List;

public interface TriggerRuntimeService extends Service {
  void executeAll(List<CompiledAction> actions, LogicContext ctx);
}