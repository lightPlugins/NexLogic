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

@Dependencies({
    TriggerBusService.class
})
public final class JoinTriggerType implements ServiceListener {

  private final ServiceAccessor services;
  private final TriggerBusService triggerBus;

  public JoinTriggerType(PaperPluginService core) {
    this.services = core.plugin().services();
    this.triggerBus = services.getService(TriggerBusService.class);
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    Player player = e.getPlayer();

    LogicContext ctx = new LogicContext("join");

    ctx.put(BukkitContextKeys.PLAYER, player);
    ctx.put(BukkitContextKeys.WORLD, player.getWorld());
    ctx.put(BukkitContextKeys.LOCATION, player.getLocation());

    triggerBus.fire("join", ctx);
  }
}