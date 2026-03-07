package io.nexstudios.nexlogic.common.types.triggers;

import io.nexstudios.framework.paper.services.ServiceListener;
import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.nexlogic.common.services.triggers.bus.TriggerBusService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;

public final class JoinTrigger implements TriggerSource, ServiceListener {

  private final TriggerBusService triggerBus;

  public JoinTrigger(PaperPluginService core) {
    this.triggerBus = core.plugin().services().getService(TriggerBusService.class);
  }

  @Override
  public String id() {
    return "join";
  }

  @Override
  public void enable(PaperPluginService core) {
    Bukkit.getPluginManager().registerEvents(this, core.plugin());
  }

  @Override
  public void disable() {
    HandlerList.unregisterAll(this);
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    var ctx = new LogicContext(id(), e.getPlayer().getUniqueId().toString());
    triggerBus.fire(id(), ctx);
  }
}