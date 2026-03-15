package io.nexstudios.nexlogic.bukkit.effects.conditions;

import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextKeys;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.effects.types.ConditionTypeService;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;

import java.util.Set;

@Slf4j
@Dependencies({
    LoggerService.class
})
public final class HasAgeConditionType implements ConditionTypeService {

  private final LoggerService logger;

  public HasAgeConditionType(ServiceAccessor services) {
    this.logger = services.getService(LoggerService.class);
  }

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

    logger.logger().info("Creating has-age condition with age: " + expectedAge);

    return ctx -> {
      if (ctx == null) return false;

      Block block = ctx.get(BukkitContextKeys.BLOCK).orElse(null);
      if (block == null) {
        logger.logger().info("No block found in context for has-age condition");
        return false;
      }

      if (!(block.getBlockData() instanceof Ageable ageable))  {
        logger.logger().info("Block data is not ageable for has-age condition");
        return false;
      }

      logger.logger().info("Block age: " + ageable.getAge() + " / Expected: " + expectedAge);
      return ageable.getAge() == expectedAge;
    };
  }
}