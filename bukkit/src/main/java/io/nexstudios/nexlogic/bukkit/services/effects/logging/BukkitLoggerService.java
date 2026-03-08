package io.nexstudios.nexlogic.bukkit.services.effects.logging;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;

import java.util.Objects;
import java.util.logging.Logger;

public final class BukkitLoggerService implements LoggerService {

  private final Logger logger;

  public BukkitLoggerService(PaperPluginService core) {
    Objects.requireNonNull(core, "core");
    this.logger = core.plugin().getLogger();
  }

  @Override
  public Logger logger() {
    return logger;
  }
}