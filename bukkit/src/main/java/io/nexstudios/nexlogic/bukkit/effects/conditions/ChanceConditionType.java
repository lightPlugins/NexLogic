package io.nexstudios.nexlogic.bukkit.effects.conditions;

import io.nexstudios.nexlogic.bukkit.services.expression.ExpressionService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.effects.types.ConditionTypeService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Dependencies({
    ExpressionService.class
})
public final class ChanceConditionType implements ConditionTypeService {

  private final ExpressionService expressions;

  public ChanceConditionType(ServiceAccessor accessor) {
    this.expressions = accessor.getService(ExpressionService.class);
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
    final String chanceExpr = args == null ? "0" : args.getString("chance", "0");

    return ctx -> {
      double percent = expressions.evaluate(chanceExpr, ctx);
      double chance = percent / 100.0;

      // Clamp to [0, 1]
      if (chance < 0.0) chance = 0.0;
      if (chance > 1.0) chance = 1.0;

      return ThreadLocalRandom.current().nextDouble() < chance;
    };
  }
}