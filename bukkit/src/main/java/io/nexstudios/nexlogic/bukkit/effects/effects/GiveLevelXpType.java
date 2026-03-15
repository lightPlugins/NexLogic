package io.nexstudios.nexlogic.bukkit.effects.effects;

import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.runtime.EffectInstance;
import io.nexstudios.nexlogic.common.effects.types.EffectTypeService;

public class GiveLevelXpType implements EffectTypeService {

  @Override
  public String id() {
    return "give-level-xp";
  }

  @Override
  public EffectInstance create(ConfigSection args) {
    final String msg = args == null ? "" : args.getString("level-name", "");
    final double amount = args == null ? 0.0 : args.getDouble("amount", 0.0);

    return null;
  }
}
