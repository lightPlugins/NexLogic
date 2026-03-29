package io.nexstudios.nexlogic.common.services.runtime;

import io.nexstudios.nexlogic.common.effects.model.CompiledAction;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

public final class DefaultActionRuntimeService implements ActionRuntimeService {

  private final LoggerService loggerService;

  public DefaultActionRuntimeService(ServiceAccessor accessor) {
    this.loggerService = accessor.getService(LoggerService.class);
  }

  @Override
  public void execute(CompiledAction action, LogicContext ctx) {
    for (var cond : action.conditions()) {
      boolean ok;
      try {
        ok = cond.test(ctx);
      } catch (Throwable t) {
        loggerService.logger().severe("Condition threw exception in action '" + action.id() + "': " + t.getClass().getSimpleName() + " " + t.getMessage());
        return;
      }
      if (!ok) return;
    }

    for (var eff : action.effects()) {
      try {
        eff.run(ctx);
      } catch (Throwable t) {
        loggerService.logger().severe("Effect threw exception in action '" + action.id() + "': " + t.getClass().getSimpleName() + " " + t.getMessage());
      }
    }
  }
}