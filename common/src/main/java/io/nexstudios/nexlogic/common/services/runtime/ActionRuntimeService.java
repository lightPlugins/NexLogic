package io.nexstudios.nexlogic.common.services.runtime;

import io.nexstudios.nexlogic.common.effects.model.CompiledAction;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.serviceregistry.di.Service;

public interface ActionRuntimeService extends Service {
  void execute(CompiledAction action, LogicContext ctx);
}