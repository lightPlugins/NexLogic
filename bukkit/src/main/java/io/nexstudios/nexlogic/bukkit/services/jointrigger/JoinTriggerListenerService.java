package io.nexstudios.nexlogic.bukkit.services.jointrigger;

import io.nexstudios.framework.paper.services.ServiceListener;
import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.nexlogic.common.services.triggers.bus.TriggerBusService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

@Dependencies({
    TriggerBusService.class
})
public final class JoinTriggerListenerService implements ServiceListener {

  private final ServiceAccessor services;
  private final TriggerBusService triggerBus;

  public JoinTriggerListenerService(PaperPluginService core) {
    this.services = core.plugin().services();
    this.triggerBus = services.getService(TriggerBusService.class);
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    var ctx = new LogicContext("join", e.getPlayer().getUniqueId().toString());
    triggerBus.fire("join", ctx);
  }
}