package io.nexstudios.nexlogic.bukkit.effects.trigger;

import io.nexstudios.framework.paper.services.ServiceListener;
import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextKeys;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.services.triggers.bus.TriggerBusService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Set;

@Dependencies({
    TriggerBusService.class
})
public final class PlayerJoinTriggerType implements ServiceListener {

  private static final String TRIGGER_ID = "player-join";

  private final ServiceAccessor services;
  private final TriggerBusService triggerBus;

  public PlayerJoinTriggerType(PaperPluginService core) {
    this.services = core.plugin().services();
    this.triggerBus = services.getService(TriggerBusService.class);
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    Player player = e.getPlayer();

    LogicContext ctx = new LogicContext(TRIGGER_ID);
    ctx.putAll(
        LogicContext.entry(BukkitContextKeys.PLAYER, player),
        LogicContext.entry(BukkitContextKeys.WORLD, player.getWorld()),
        LogicContext.entry(BukkitContextKeys.LOCATION, player.getLocation())
    );

    triggerBus.fire("join", ctx);
  }
}