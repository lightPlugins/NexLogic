package io.nexstudios.nexlogic.bukkit.types.filters;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.bukkit.services.context.BukkitContextResolverService;
import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.config.LogicData;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.nexlogic.common.types.FilterTypeService;
import io.nexstudios.serviceregistry.di.Dependencies;
import org.bukkit.World;

import java.util.Set;
import java.util.function.Predicate;

@Dependencies({
    BukkitContextResolverService.class
})
public final class WorldsFilterType implements FilterTypeService {

  private final BukkitContextResolverService resolver;

  public WorldsFilterType(PaperPluginService core) {
    this.resolver = core.plugin().services().getService(BukkitContextResolverService.class);
  }

  @Override
  public String id() {
    return "worlds";
  }

  @Override
  public Set<ContextCapability> requiredCapabilities() {
    return Set.of(ContextCapability.WORLD);
  }

  @Override
  public Predicate<LogicContext> compile(String triggerId, ConfigSection args) {
    LogicData data = new LogicData(args);
    Set<String> allow = data.getStringSet("allow");
    Set<String> deny = data.getStringSet("deny");

    if (allow.isEmpty() && deny.isEmpty()) return ctx -> true;

    return ctx -> {
      World world = resolver.world(ctx).orElse(null);
      if (world == null) return false;

      String name = world.getName().toLowerCase();

      if (!deny.isEmpty() && deny.contains(name)) return false;
      if (!allow.isEmpty()) return allow.contains(name);
      return true;
    };
  }
}