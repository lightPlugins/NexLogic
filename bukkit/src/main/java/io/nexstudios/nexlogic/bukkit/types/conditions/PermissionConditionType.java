package io.nexstudios.nexlogic.bukkit.types.conditions;

import io.nexstudios.nexlogic.bukkit.services.context.BukkitContextKeys;
import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.nexlogic.common.types.ConditionTypeService;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Set;

public final class PermissionConditionType implements ConditionTypeService {

  @Override
  public String id() {
    return "permission";
  }

  @Override
  public Set<ContextCapability> requiredCapabilities() {
    return Set.of(ContextCapability.PLAYER);
  }

  @Override
  public ConditionInstance create(ConfigSection args) {
    String perm = Objects.requireNonNull(
        args == null ? null : args.getString("permission", null),
        "permission.args.permission is required"
    );

    return ctx -> ctx != null
        && ctx.get(BukkitContextKeys.PLAYER)
        .filter(Player::isOnline)
        .map(p -> p.hasPermission(perm))
        .orElse(false);
  }
}