package io.nexstudios.nexlogic.common.services.filters;

import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.config.MapConfigSection;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.services.registry.filter.FilterTypeRegistryService;
import io.nexstudios.nexlogic.common.services.triggers.schema.TriggerContextSchemaService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.util.function.Predicate;

@Dependencies({
    FilterTypeRegistryService.class,
    TriggerContextSchemaService.class
})
public final class DefaultFilterService implements FilterService {

  private final FilterTypeRegistryService types;
  private final TriggerContextSchemaService schema;

  public DefaultFilterService(ServiceAccessor service) {
    this.types = service.getService(FilterTypeRegistryService.class);
    this.schema = service.getService(TriggerContextSchemaService.class);
  }

  @Override
  public Predicate<LogicContext> compile(String triggerId, ConfigSection filters) {
    if (filters == null) return ctx -> true;

    String t = triggerId == null ? "" : triggerId.toLowerCase();
    var caps = schema.capabilities(t);

    Predicate<LogicContext> out = ctx -> true;

    for (String filterId : filters.getKeys(false)) {
      var type = types.resolve(filterId).orElseThrow(() ->
          new IllegalArgumentException("Unknown filter id '" + filterId + "' in filters section")
      );

      for (var required : type.requiredCapabilities()) {
        if (!caps.contains(required)) {
          throw new IllegalArgumentException(
              "Filter '" + type.id() + "' requires capability '" + required + "', " +
                  "but trigger '" + triggerId + "' does not provide it. Provided: " + caps
          );
        }
      }

      ConfigSection filterEntry = filters.getSection(filterId);
      ConfigSection args = filterEntry == null ? null : filterEntry.getSection("args");

      out = out.and(type.compile(t, args == null ? MapConfigSection.EMPTY : args));
    }

    return out;
  }
}