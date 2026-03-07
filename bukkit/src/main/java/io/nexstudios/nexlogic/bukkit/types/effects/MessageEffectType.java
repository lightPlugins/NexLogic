package io.nexstudios.nexlogic.bukkit.types.effects;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.bukkit.services.context.BukkitContextKeys;
import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.runtime.EffectInstance;
import io.nexstudios.nexlogic.common.services.executor.MainThreadExecutorService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.nexlogic.common.types.EffectTypeService;
import org.bukkit.entity.Player;

import java.util.Set;

public final class MessageEffectType implements EffectTypeService {

  private final MainThreadExecutorService mainThread;

  public MessageEffectType(PaperPluginService core) {
    this.mainThread = core.plugin().services().getService(MainThreadExecutorService.class);
  }

  @Override
  public String id() {
    return "message";
  }

  @Override
  public Set<ContextCapability> requiredCapabilities() {
    return Set.of(ContextCapability.PLAYER);
  }

  @Override
  public EffectInstance create(ConfigSection args) {
    String message = args == null ? "" : args.getString("message", "");

    return ctx -> {
      if (ctx == null) return;

      Player p = ctx.get(BukkitContextKeys.PLAYER).orElse(null);
      if (p == null || !p.isOnline()) return;

      Runnable send = () -> {
        if (p.isOnline()) p.sendMessage(message);
      };

      if (mainThread.isMainThread()) send.run();
      else mainThread.execute(send);
    };
  }
}