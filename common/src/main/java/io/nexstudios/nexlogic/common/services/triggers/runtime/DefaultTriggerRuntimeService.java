package io.nexstudios.nexlogic.common.services.triggers.runtime;

import io.nexstudios.nexlogic.common.effects.model.CompiledAction;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.services.runtime.ActionRuntimeService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.util.List;

@Dependencies({
    ActionRuntimeService.class
})
public final class DefaultTriggerRuntimeService implements TriggerRuntimeService {

  private final ActionRuntimeService runtime;

  public DefaultTriggerRuntimeService(ServiceAccessor service) {
    this.runtime = service.getService(ActionRuntimeService.class);
  }

  @Override
  public void executeAll(List<CompiledAction> actions, LogicContext ctx) {
    for (var a : actions) {
      runtime.execute(a, ctx);
    }
  }
}