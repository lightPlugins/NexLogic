package io.nexstudios.nexlogic.bukkit.types.conditions;

import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.nexlogic.common.types.ConditionTypeService;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class ChanceConditionType implements ConditionTypeService {

  @Override
  public String id() {
    return "chance";
  }

  @Override
  public Set<ContextCapability> requiredCapabilities() {
    return Set.of();
  }

  @Override
  public ConditionInstance create(ConfigSection args) {
    double raw = args == null ? 0.0 : args.getDouble("chance", 0.0);

    // Accept both 0.0-1.0 and 0-100 formats.
    double chance = raw > 1.0 ? (raw / 100.0) : raw;

    // Clamp to [0, 1]
    if (chance < 0.0) chance = 0.0;
    if (chance > 1.0) chance = 1.0;

    double finalChance = chance;
    return ctx -> ThreadLocalRandom.current().nextDouble() < finalChance;
  }
}