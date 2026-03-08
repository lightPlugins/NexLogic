package io.nexstudios.nexlogic.bukkit.services.effects.listeners;

import io.nexstudios.framework.paper.services.ServiceListener;
import io.nexstudios.nexlogic.bukkit.services.effects.blocks.PlayerPlacedBlockTrackerService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Dependencies({
    PlayerPlacedBlockTrackerService.class
})
public final class PistonMoveListener implements ServiceListener {

  private final PlayerPlacedBlockTrackerService tracker;

  public PistonMoveListener(ServiceAccessor services) {
    this.tracker = services.getService(PlayerPlacedBlockTrackerService.class);
  }

  @EventHandler(ignoreCancelled = true)
  public void onExtend(BlockPistonExtendEvent event) {
    Block piston = event.getBlock();
    BlockFace dir = event.getDirection();

    List<Block> moved = new ArrayList<>(event.getBlocks());

    // EXTEND pushes blocks away from piston: process farthest -> nearest
    moved.sort(Comparator.<Block>comparingInt(b -> projection(piston, b, dir)).reversed());

    for (Block from : moved) {
      Block to = from.getRelative(dir);
      tracker.move(from, to);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onRetract(BlockPistonRetractEvent event) {
    if (!event.isSticky()) return;

    Block piston = event.getBlock();
    BlockFace dir = event.getDirection();
    BlockFace moveDir = dir.getOppositeFace(); // retract pulls blocks towards piston

    List<Block> moved = new ArrayList<>(event.getBlocks());

    // RETRACT pulls blocks towards piston: process nearest -> farthest
    moved.sort(Comparator.comparingInt(b -> projection(piston, b, dir)));

    for (Block from : moved) {
      Block to = from.getRelative(moveDir);
      tracker.move(from, to);
    }
  }

  /**
   * Returns a sortable distance along piston direction:
   * larger value = further away from piston along dir.
   */
  private static int projection(Block piston, Block b, BlockFace dir) {
    int dx = b.getX() - piston.getX();
    int dy = b.getY() - piston.getY();
    int dz = b.getZ() - piston.getZ();
    return dx * dir.getModX() + dy * dir.getModY() + dz * dir.getModZ();
  }
}