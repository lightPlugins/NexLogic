package io.nexstudios.nexlogic.bukkit.services.effects.listeners;

import io.nexstudios.framework.paper.services.ServiceListener;
import io.nexstudios.nexlogic.bukkit.services.effects.blocks.PlayerPlacedBlockTrackerService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

@Dependencies({
    PlayerPlacedBlockTrackerService.class
})
public final class ExplosionCleanupListener implements ServiceListener {

  private final PlayerPlacedBlockTrackerService tracker;

  public ExplosionCleanupListener(ServiceAccessor services) {
    this.tracker = services.getService(PlayerPlacedBlockTrackerService.class);
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityExplode(EntityExplodeEvent event) {
    for (Block b : event.blockList()) tracker.unmark(b);
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockExplode(BlockExplodeEvent event) {
    for (Block b : event.blockList()) tracker.unmark(b);
  }
}