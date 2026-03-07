package io.nexstudios.nexlogic.common.services.triggers.runtime;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.model.CompiledAction;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.nexlogic.common.services.runtime.ActionRuntimeService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.util.List;

@Dependencies({
    ActionRuntimeService.class
})
public final class DefaultTriggerRuntimeService implements TriggerRuntimeService {

  private final ServiceAccessor services;
  private final ActionRuntimeService runtime;

  public DefaultTriggerRuntimeService(PaperPluginService core) {
    this.services = core.plugin().services();
    this.runtime = services.getService(ActionRuntimeService.class);
  }

  @Override
  public void executeAll(List<CompiledAction> actions, LogicContext ctx) {
    for (var a : actions) {
      runtime.execute(a, ctx);
    }
  }
}