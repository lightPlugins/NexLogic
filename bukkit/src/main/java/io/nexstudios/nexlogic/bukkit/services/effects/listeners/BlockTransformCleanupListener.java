package io.nexstudios.nexlogic.bukkit.services.effects.listeners;

import io.nexstudios.framework.paper.services.ServiceListener;
import io.nexstudios.nexlogic.bukkit.services.effects.blocks.PlayerPlacedBlockTrackerService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.world.StructureGrowEvent;

@Dependencies({
    PlayerPlacedBlockTrackerService.class
})
public final class BlockTransformCleanupListener implements ServiceListener {

  private final PlayerPlacedBlockTrackerService tracker;

  public BlockTransformCleanupListener(ServiceAccessor services) {
    this.tracker = services.getService(PlayerPlacedBlockTrackerService.class);
  }

  @EventHandler(ignoreCancelled = true)
  public void onGrow(BlockGrowEvent event) {
    tracker.unmark(event.getBlock());
  }

  @EventHandler(ignoreCancelled = true)
  public void onSpread(BlockSpreadEvent event) {
    // Target changes
    tracker.unmark(event.getBlock());
  }

  @EventHandler(ignoreCancelled = true)
  public void onFade(BlockFadeEvent event) {
    tracker.unmark(event.getBlock());
  }

  @EventHandler(ignoreCancelled = true)
  public void onForm(BlockFormEvent event) {
    tracker.unmark(event.getBlock());
  }

  @EventHandler(ignoreCancelled = true)
  public void onLeavesDecay(LeavesDecayEvent event) {
    tracker.unmark(event.getBlock());
  }

  @EventHandler(ignoreCancelled = true)
  public void onFluidFromTo(BlockFromToEvent event) {
    // Fluid flow replaces/updates the TO block
    Block to = event.getToBlock();
    tracker.unmark(to);
  }

  @EventHandler(ignoreCancelled = true)
  public void onStructureGrow(StructureGrowEvent event) {
    for (BlockState state : event.getBlocks()) {
      if (state == null) continue;
      tracker.unmark(state.getBlock());
    }
  }
}