package io.nexstudios.nexlogic.common.types.conditions;

import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.services.types.ConditionTypeService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public final class PermissionConditionType implements ConditionTypeService {

  @Override
  public String id() {
    return "permission";
  }

  @Override
  public ConditionInstance create(ConfigSection args) {
    String perm = Objects.requireNonNull(args.getString("permission", null), "permission.args.permission is required");
    return ctx -> ctx.actorUuid()
        .map(u -> Bukkit.getPlayer(UUID.fromString(u)))
        .filter(Player::isOnline)
        .map(p -> p.hasPermission(perm))
        .orElse(false);
  }
}