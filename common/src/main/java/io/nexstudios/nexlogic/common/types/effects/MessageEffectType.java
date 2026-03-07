package io.nexstudios.nexlogic.common.types.effects;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.runtime.EffectInstance;
import io.nexstudios.nexlogic.common.services.executor.MainThreadExecutorService;
import io.nexstudios.nexlogic.common.services.types.effect.EffectTypeService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

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
  public EffectInstance create(ConfigSection args) {
    String message = args.getString("message", "");
    return ctx -> ctx.actorUuid().ifPresent(u -> {
      Runnable send = () -> {
        Player p = Bukkit.getPlayer(UUID.fromString(u));
        if (p != null && p.isOnline()) p.sendMessage(message);
      };
      if (mainThread.isMainThread()) send.run();
      else mainThread.execute(send);
    });
  }
}