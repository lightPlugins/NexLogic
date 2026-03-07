package io.nexstudios.nexlogic.bukkit.types.effects;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.runtime.EffectInstance;
import io.nexstudios.nexlogic.common.types.EffectTypeService;

import java.util.logging.Logger;

public final class LogEffectType implements EffectTypeService {

  private final Logger logger;

  public LogEffectType(PaperPluginService core) {
    this.logger = core.plugin().getLogger();
  }

  @Override
  public String id() {
    return "log";
  }

  @Override
  public EffectInstance create(ConfigSection args) {
    String msg = args == null ? "" : args.getString("message", "");
    return ctx -> logger.info("[NexLogic] " + msg);
  }
}