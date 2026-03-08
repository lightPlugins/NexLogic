package io.nexstudios.nexlogic.bukkit.effects.conditions;

import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextKeys;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.PlaceholderRuntimeService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.effects.types.ConditionTypeService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Set;

@Dependencies({
    PlaceholderRuntimeService.class
})
public final class PermissionConditionType implements ConditionTypeService {

  private final PlaceholderRuntimeService placeholders;

  public PermissionConditionType(PaperPluginService core) {
    this.placeholders = core.plugin().services().getService(PlaceholderRuntimeService.class);
  }

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
    return ctx -> {
      ConfigSection resolved = placeholders.resolveSection(args, ctx);

      String perm = Objects.requireNonNull(
          resolved.getString("permission", null),
          "permission.args.permission is required"
      );

      Player p = ctx == null ? null : ctx.get(BukkitContextKeys.PLAYER).orElse(null);
      if (p == null || !p.isOnline()) return false;

      return p.hasPermission(perm);
    };
  }
}