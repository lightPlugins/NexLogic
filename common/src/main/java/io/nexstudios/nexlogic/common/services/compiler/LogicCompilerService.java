package io.nexstudios.nexlogic.common.services.compiler;

import io.nexstudios.nexlogic.common.model.ActionDefinition;
import io.nexstudios.nexlogic.common.model.CompiledAction;
import io.nexstudios.serviceregistry.di.Service;

public interface LogicCompilerService extends Service {
  CompiledAction compile(ActionDefinition def);
}