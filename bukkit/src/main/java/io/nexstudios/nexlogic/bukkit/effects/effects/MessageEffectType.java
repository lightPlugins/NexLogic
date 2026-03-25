package io.nexstudios.nexlogic.bukkit.effects.effects;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.languageservice.service.component.ComponentService;
import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextKeys;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.PlaceholderRuntimeService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.runtime.EffectInstance;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.MainThreadExecutorService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.nexlogic.common.effects.types.EffectTypeService;
import io.nexstudios.serviceregistry.di.Dependencies;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.Set;

@Dependencies({
    MainThreadExecutorService.class,
    PlaceholderRuntimeService.class,
    ComponentService.class
})
public final class MessageEffectType implements EffectTypeService {

  private final MainThreadExecutorService mainThread;
  private final PlaceholderRuntimeService placeholders;
  private final ComponentService componentService;

  public MessageEffectType(PaperPluginService core) {
    this.mainThread = core.plugin().services().getService(MainThreadExecutorService.class);
    this.placeholders = core.plugin().services().getService(PlaceholderRuntimeService.class);
    this.componentService = core.plugin().services().getService(ComponentService.class);
  }

  @Override
  public String id() {
    return "send-message";
  }

  @Override
  public Set<ContextCapability> requiredCapabilities() {
    return Set.of(ContextCapability.PLAYER);
  }

  @Override
  public EffectInstance create(ConfigSection args) {
    String message = args == null ? "" : args.getString("message", "");
    boolean withPrefix = args != null && args.getBoolean("with-prefix", false);
    String configPath = args == null ? null : args.getString("config-path", null);

    return ctx -> {
      if (ctx == null) return;

      Player p = ctx.get(BukkitContextKeys.PLAYER).orElse(null);
      if (p == null || !p.isOnline()) return;

      Runnable send = () -> {
        if (!p.isOnline()) return;

        if (configPath == null) {
          p.sendMessage(MiniMessage.miniMessage().deserialize(placeholders.resolve(message, ctx)));
          return;
        }

        Component component = componentService
            .builder(p, configPath, "Unknown", withPrefix)
            .string(string -> placeholders.resolve(string, ctx))
            .build();
        p.sendMessage(component);
      };

      if (mainThread.isMainThread()) send.run();
      else mainThread.execute(send);
    };
  }
}