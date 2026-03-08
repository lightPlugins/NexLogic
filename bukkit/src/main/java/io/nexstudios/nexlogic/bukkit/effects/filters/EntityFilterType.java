package io.nexstudios.nexlogic.bukkit.effects.filters;

import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextResolverService;
import io.nexstudios.nexlogic.bukkit.services.effects.entities.EntityKeyService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.PlaceholderRuntimeService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.config.LogicData;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.effects.types.FilterTypeService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.entity.Entity;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Dependencies({
    BukkitContextResolverService.class,
    PlaceholderRuntimeService.class,
    EntityKeyService.class
})
public class EntityFilterType implements FilterTypeService {

  private final BukkitContextResolverService resolver;
  private final PlaceholderRuntimeService placeholders;
  private final EntityKeyService entityKeys;

  public EntityFilterType(ServiceAccessor service) {
    this.resolver = service.getService(BukkitContextResolverService.class);
    this.placeholders = service.getService(PlaceholderRuntimeService.class);
    this.entityKeys = service.getService(EntityKeyService.class);
  }

  @Override
  public String id() {
    return "entities";
  }

  @Override
  public Set<ContextCapability> requiredCapabilities() {
    return Set.of(ContextCapability.ENTITY);
  }

  @Override
  public Predicate<LogicContext> compile(String triggerId, ConfigSection args) {
    return ctx -> {
      ConfigSection resolvedArgs = placeholders.resolveSection(args, ctx);
      LogicData data = new LogicData(resolvedArgs);

      Set<String> entitiesRaw = data.getStringSet("entities");
      boolean inverted = data.getBoolean("inverted", false);

      if (entitiesRaw.isEmpty()) return true;



      Set<String> allowed = entitiesRaw.stream()
          .map(entityKeys::normalize)
          .filter(s -> !s.isBlank())
          .collect(Collectors.toUnmodifiableSet());

      Entity entity = resolver.entity(ctx).orElse(null);
      if (entity == null) return false;

      boolean match = entityKeys.matches(entity, allowed);
      return inverted != match;
    };
  }
}
