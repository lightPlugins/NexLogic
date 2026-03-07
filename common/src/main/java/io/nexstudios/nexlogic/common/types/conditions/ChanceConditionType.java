package io.nexstudios.nexlogic.common.types.conditions;

import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.services.types.ConditionTypeService;

import java.util.concurrent.ThreadLocalRandom;

public final class ChanceConditionType implements ConditionTypeService {

  @Override
  public String id() {
    return "chance";
  }

  @Override
  public ConditionInstance create(ConfigSection args) {
    double chance = args.getDouble("chance", 0.0);
    return ctx -> ThreadLocalRandom.current().nextDouble() < chance;
  }
}