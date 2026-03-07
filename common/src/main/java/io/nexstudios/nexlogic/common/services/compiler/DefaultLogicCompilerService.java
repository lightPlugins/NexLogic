package io.nexstudios.nexlogic.common.services.compiler;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.config.MapConfigSection;
import io.nexstudios.nexlogic.common.model.ActionDefinition;
import io.nexstudios.nexlogic.common.model.CompiledAction;
import io.nexstudios.nexlogic.common.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.runtime.EffectInstance;
import io.nexstudios.nexlogic.common.services.registry.condition.ConditionTypeRegistryService;
import io.nexstudios.nexlogic.common.services.registry.effect.EffectTypeRegistryService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.util.ArrayList;
import java.util.List;

@Dependencies({
    EffectTypeRegistryService.class,
    ConditionTypeRegistryService.class
})
public final class DefaultLogicCompilerService implements LogicCompilerService {

  private final ServiceAccessor services;
  private final EffectTypeRegistryService effects;
  private final ConditionTypeRegistryService conditions;

  public DefaultLogicCompilerService(PaperPluginService core) {
    this.services = core.plugin().services();
    this.effects = services.getService(EffectTypeRegistryService.class);
    this.conditions = services.getService(ConditionTypeRegistryService.class);
  }

  @Override
  public CompiledAction compile(ActionDefinition def) {
    List<ConditionInstance> compiledConditions = new ArrayList<>();
    for (int i = 0; i < def.conditions().size(); i++) {
      final int idx = i;
      ConfigSection entry = def.conditions().get(i);

      final String id = entry.getString("id", null);
      if (id == null) {
        throw new IllegalArgumentException("Condition entry missing 'id' at index " + idx + " in action '" + def.id() + "'");
      }

      var svc = conditions.resolve(id).orElseThrow(() ->
          new IllegalArgumentException("Unknown condition id '" + id + "' in action '" + def.id() + "' at index " + idx)
      );

      ConfigSection args = entry.getSection("args");
      compiledConditions.add(svc.create(args == null ? MapConfigSection.EMPTY : args));
    }

    List<EffectInstance> compiledEffects = new ArrayList<>();
    for (int i = 0; i < def.effects().size(); i++) {
      final int idx = i;
      ConfigSection entry = def.effects().get(i);

      final String id = entry.getString("id", null);
      if (id == null) {
        throw new IllegalArgumentException("Effect entry missing 'id' at index " + idx + " in action '" + def.id() + "'");
      }

      var svc = effects.resolve(id).orElseThrow(() ->
          new IllegalArgumentException("Unknown effect id '" + id + "' in action '" + def.id() + "' at index " + idx)
      );

      ConfigSection args = entry.getSection("args");
      compiledEffects.add(svc.create(args == null ? MapConfigSection.EMPTY : args));
    }

    return new CompiledAction(def.id(), def.triggers(), compiledConditions, compiledEffects);
  }
}