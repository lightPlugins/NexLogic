package io.nexstudios.nexlogic.common.services.runtime;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.model.CompiledAction;
import io.nexstudios.nexlogic.common.model.LogicContext;

import java.util.logging.Logger;

public final class DefaultActionRuntimeService implements ActionRuntimeService {

  private final Logger logger;

  public DefaultActionRuntimeService(PaperPluginService core) {
    this.logger = core.plugin().getLogger();
  }

  @Override
  public void execute(CompiledAction action, LogicContext ctx) {
    for (var cond : action.conditions()) {
      boolean ok;
      try {
        ok = cond.test(ctx);
      } catch (Throwable t) {
        logger.severe("Condition threw an exception in action '" + action.id() + "': " + t.getMessage());
        t.printStackTrace();
        return;
      }
      if (!ok) return;
    }

    for (var eff : action.effects()) {
      try {
        eff.run(ctx);
      } catch (Throwable t) {
        logger.severe("Effect threw an exception in action '" + action.id() + "': " + t.getMessage());
        t.printStackTrace();
      }
    }
  }
}