package io.nexstudios.nexlogic.bukkit.effects.trigger;

import io.nexstudios.framework.paper.services.ServiceListener;
import io.nexstudios.nexlogic.bukkit.services.effects.blocks.PlayerPlacedBlockTrackerService;
import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextKeys;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.services.triggers.bus.TriggerBusService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

@Dependencies({
    PlayerPlacedBlockTrackerService.class
})
public final class BlockPlaceTriggerType implements ServiceListener {

  private static final String TRIGGER_ID = "block-place";
  private final TriggerBusService triggerBus;
  private final PlayerPlacedBlockTrackerService tracker;

  public BlockPlaceTriggerType(ServiceAccessor accessor) {
    this.triggerBus = accessor.getService(TriggerBusService.class);
    this.tracker = accessor.getService(PlayerPlacedBlockTrackerService.class);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlace(BlockPlaceEvent event) {
    Player player = event.getPlayer();
    Block block = event.getBlockPlaced();

    // Mark first, so conditions/effects triggered on place-block can already see it if needed.
    // only in Survival/Adventure game modes
    if(!event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
      tracker.markPlaced(block);
    }

    LogicContext ctx = new LogicContext(TRIGGER_ID);

    ctx.putAll(
        LogicContext.entry(BukkitContextKeys.PLAYER, player),
        LogicContext.entry(BukkitContextKeys.BLOCK, block),
        LogicContext.entry(BukkitContextKeys.LOCATION, block.getLocation()),
        LogicContext.entry(BukkitContextKeys.WORLD, block.getWorld())
    );

    triggerBus.fire(TRIGGER_ID, ctx);
  }
}