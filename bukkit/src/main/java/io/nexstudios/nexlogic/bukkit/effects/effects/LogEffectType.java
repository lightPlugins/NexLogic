package io.nexstudios.nexlogic.bukkit.effects.effects;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.runtime.EffectInstance;
import io.nexstudios.nexlogic.common.effects.types.EffectTypeService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.PlaceholderRuntimeService;
import io.nexstudios.serviceregistry.di.Dependencies;

import java.util.logging.Logger;

@Dependencies({
    PlaceholderRuntimeService.class
})
public final class LogEffectType implements EffectTypeService {

  private final Logger logger;
  private final PlaceholderRuntimeService placeholders;

  public LogEffectType(PaperPluginService core) {
    this.logger = core.plugin().getLogger();
    this.placeholders = core.plugin().services().getService(PlaceholderRuntimeService.class);
  }

  @Override
  public String id() {
    return "log";
  }

  @Override
  public EffectInstance create(ConfigSection args) {
    String msg = args == null ? "" : args.getString("message", "");
    return ctx -> logger.info(placeholders.resolve(msg, ctx));
  }
}