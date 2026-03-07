package io.nexstudios.nexlogic.common.services.runtime;

import io.nexstudios.nexlogic.common.model.CompiledAction;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.serviceregistry.di.Service;

public interface ActionRuntimeService extends Service {
  void execute(CompiledAction action, LogicContext ctx);
}