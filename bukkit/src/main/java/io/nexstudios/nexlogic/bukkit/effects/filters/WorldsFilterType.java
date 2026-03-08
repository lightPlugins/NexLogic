package io.nexstudios.nexlogic.bukkit.effects.filters;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextResolverService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.PlaceholderRuntimeService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.config.LogicData;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.effects.types.FilterTypeService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.World;

import java.util.Set;
import java.util.function.Predicate;

@Dependencies({
    BukkitContextResolverService.class,
    PlaceholderRuntimeService.class
})
public final class WorldsFilterType implements FilterTypeService {

  private final BukkitContextResolverService resolver;
  private final PlaceholderRuntimeService placeholders;

  public WorldsFilterType(ServiceAccessor service) {
    this.resolver = service.getService(BukkitContextResolverService.class);
    this.placeholders = service.getService(PlaceholderRuntimeService.class);

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
    return ctx -> {
      ConfigSection resolved = placeholders.resolveSection(args, ctx);
      LogicData data = new LogicData(resolved);

      Set<String> allow = data.getStringSet("allow");
      Set<String> deny = data.getStringSet("deny");

      if (allow.isEmpty() && deny.isEmpty()) return true;

      World world = resolver.world(ctx).orElse(null);
      if (world == null) return false;

      String name = world.getName().toLowerCase();

      if (!deny.isEmpty() && deny.contains(name)) return false;
      if (!allow.isEmpty()) return allow.contains(name);
      return true;
    };
  }
}