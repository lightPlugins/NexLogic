package io.nexstudios.nexlogic.bukkit.effects.conditions;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.PlaceholderRuntimeService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.effects.types.ConditionTypeService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.serviceregistry.di.Dependencies;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Dependencies({
    PlaceholderRuntimeService.class
})
public final class ChanceConditionType implements ConditionTypeService {

  private final PlaceholderRuntimeService placeholders;

  public ChanceConditionType(PaperPluginService core) {
    this.placeholders = core.plugin().services().getService(PlaceholderRuntimeService.class);
  }

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
    return ctx -> {
      ConfigSection resolved = placeholders.resolveSection(args, ctx);

      double raw = resolved.getDouble("chance", 0.0);

      // Accept both 0.0-1.0 and 0-100 formats.
      double chance = raw > 1.0 ? (raw / 100.0) : raw;

      // Clamp to [0, 1]
      if (chance < 0.0) chance = 0.0;
      if (chance > 1.0) chance = 1.0;

      return ThreadLocalRandom.current().nextDouble() < chance;
    };
  }
}