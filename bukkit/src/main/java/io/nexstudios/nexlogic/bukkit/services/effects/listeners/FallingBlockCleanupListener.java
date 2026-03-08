package io.nexstudios.nexlogic.bukkit.services.effects.listeners;

import io.nexstudios.framework.paper.services.ServiceListener;
import io.nexstudios.nexlogic.bukkit.services.effects.blocks.PlayerPlacedBlockTrackerService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

@Dependencies({
    PlayerPlacedBlockTrackerService.class
})
public final class FallingBlockCleanupListener implements ServiceListener {

  private final PlayerPlacedBlockTrackerService tracker;

  public FallingBlockCleanupListener(ServiceAccessor services) {
    this.tracker = services.getService(PlayerPlacedBlockTrackerService.class);
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityChangeBlock(EntityChangeBlockEvent event) {
    if (!(event.getEntity() instanceof FallingBlock)) return;

    // When a block turns into a falling entity, the block at that location becomes AIR.
    if (event.getTo() == Material.AIR) {
      tracker.unmark(event.getBlock());
    }
  }
}