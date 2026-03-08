package io.nexstudios.nexlogic.bukkit.effects.conditions;

import io.nexstudios.nexlogic.bukkit.services.effects.blocks.PlayerPlacedBlockTrackerService;
import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextKeys;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.config.LogicData;
import io.nexstudios.nexlogic.common.effects.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.effects.types.ConditionTypeService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.block.Block;

import java.util.Set;

@Dependencies({
    PlayerPlacedBlockTrackerService.class
})
public final class PlayerPlacedConditionType implements ConditionTypeService {

  private final PlayerPlacedBlockTrackerService tracker;

  public PlayerPlacedConditionType(ServiceAccessor services) {
    this.tracker = services.getService(PlayerPlacedBlockTrackerService.class);
  }

  @Override
  public String id() {
    return "player-placed";
  }

  @Override
  public Set<ContextCapability> requiredCapabilities() {
    return Set.of(ContextCapability.BLOCK);
  }

  @Override
  public ConditionInstance create(ConfigSection args) {
    LogicData data = new LogicData(args);
    boolean expected = data.getBoolean("value", true);

    return ctx -> {
      if (ctx == null) return false;

      Block block = ctx.get(BukkitContextKeys.BLOCK).orElse(null);
      if (block == null) return false;

      boolean actual = tracker.isPlayerPlaced(block);
      return actual == expected;
    };
  }
}