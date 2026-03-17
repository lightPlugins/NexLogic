package io.nexstudios.nexlogic.bukkit.effects.conditions;

import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextKeys;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.effects.types.ConditionTypeService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;

import java.util.Set;

public final class HasAgeConditionType implements ConditionTypeService {

  @Override
  public String id() {
    return "has-age";
  }

  @Override
  public Set<ContextCapability> requiredCapabilities() {
    return Set.of(ContextCapability.BLOCK);
  }

  @Override
  public ConditionInstance create(ConfigSection config) {
    int expectedAge = config == null ? 0 : config.getInt("age", 0);

    return ctx -> {
      if (ctx == null) return false;

      Block block = ctx.get(BukkitContextKeys.BLOCK).orElse(null);

      if (block == null) { return false; }
      if (!(block.getBlockData() instanceof Ageable ageable)) { return false; }

      return ageable.getAge() == expectedAge;
    };
  }
}