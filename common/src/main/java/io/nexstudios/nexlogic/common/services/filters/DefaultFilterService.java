package io.nexstudios.nexlogic.common.services.filters;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.config.MapConfigSection;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.nexlogic.common.services.registry.filter.FilterTypeRegistryService;
import io.nexstudios.nexlogic.common.services.triggers.schema.TriggerContextSchemaService;

import java.util.function.Predicate;

public final class DefaultFilterService implements FilterService {

  private final FilterTypeRegistryService types;
  private final TriggerContextSchemaService schema;

  public DefaultFilterService(PaperPluginService core) {
    var services = core.plugin().services();
    this.types = services.getService(FilterTypeRegistryService.class);
    this.schema = services.getService(TriggerContextSchemaService.class);
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