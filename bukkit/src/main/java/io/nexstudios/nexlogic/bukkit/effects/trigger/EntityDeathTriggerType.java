package io.nexstudios.nexlogic.bukkit.effects.trigger;

import io.nexstudios.framework.paper.services.ServiceListener;
import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextKeys;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.services.triggers.bus.TriggerBusService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;


@Dependencies({
    TriggerBusService.class
})
public final class EntityDeathTriggerType implements ServiceListener {

  private static final String TRIGGER_ID = "entity-death";

  private final ServiceAccessor services;
  private final TriggerBusService triggerBus;

  public EntityDeathTriggerType(PaperPluginService core) {
    this.services = core.plugin().services();
    this.triggerBus = services.getService(TriggerBusService.class);
  }

  @EventHandler
  public void onJoin(EntityDeathEvent e) {
    Player player = e.getEntity().getKiller();
    if (player == null) return;
    Entity entity = e.getEntity();
    World world = e.getEntity().getWorld();
    Location location = e.getEntity().getLocation();

    LogicContext ctx = new LogicContext(TRIGGER_ID);
    ctx.putAll(
        LogicContext.entry(BukkitContextKeys.PLAYER, player),
        LogicContext.entry(BukkitContextKeys.ENTITY, entity),
        LogicContext.entry(BukkitContextKeys.WORLD, world),
        LogicContext.entry(BukkitContextKeys.LOCATION, location)
    );

    triggerBus.fire(TRIGGER_ID, ctx);
  }
}