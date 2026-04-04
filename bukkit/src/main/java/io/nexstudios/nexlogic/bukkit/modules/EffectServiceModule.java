package io.nexstudios.nexlogic.bukkit.modules;

import io.nexstudios.nexlogic.common.services.conditions.ConditionEvaluatorService;
import io.nexstudios.nexlogic.common.services.conditions.DefaultConditionEvaluatorService;
import io.nexstudios.nexlogic.common.services.engine.DefaultLogicEngineService;
import io.nexstudios.nexlogic.common.services.engine.LogicEngineService;
import io.nexstudios.nexlogic.common.services.filters.DefaultFilterService;
import io.nexstudios.nexlogic.common.services.filters.FilterService;
import io.nexstudios.nexlogic.common.services.registry.addon.AddonRegistryService;
import io.nexstudios.nexlogic.common.services.registry.addon.DefaultAddonRegistryService;
import io.nexstudios.nexlogic.common.services.registry.condition.ConditionTypeRegistryService;
import io.nexstudios.nexlogic.common.services.registry.condition.DefaultConditionTypeRegistryService;
import io.nexstudios.nexlogic.common.services.registry.effect.DefaultEffectTypeRegistryService;
import io.nexstudios.nexlogic.common.services.registry.effect.EffectTypeRegistryService;
import io.nexstudios.nexlogic.common.services.registry.filter.DefaultFilterTypeRegistryService;
import io.nexstudios.nexlogic.common.services.registry.filter.FilterTypeRegistryService;
import io.nexstudios.nexlogic.common.services.runtime.ActionRuntimeService;
import io.nexstudios.nexlogic.common.services.runtime.DefaultActionRuntimeService;
import io.nexstudios.nexlogic.common.services.triggers.bus.DefaultTriggerBusService;
import io.nexstudios.nexlogic.common.services.triggers.bus.TriggerBusService;
import io.nexstudios.nexlogic.common.services.triggers.register.DefaultTriggerRegistrationService;
import io.nexstudios.nexlogic.common.services.triggers.register.TriggerRegistrationService;
import io.nexstudios.nexlogic.common.services.triggers.rules.DefaultTriggerRuleRegistryService;
import io.nexstudios.nexlogic.common.services.triggers.rules.TriggerRuleRegistryService;
import io.nexstudios.nexlogic.common.services.triggers.runtime.DefaultTriggerRuntimeService;
import io.nexstudios.nexlogic.common.services.triggers.runtime.TriggerRuntimeService;
import io.nexstudios.nexlogic.common.services.triggers.schema.DefaultTriggerContextSchemaService;
import io.nexstudios.nexlogic.common.services.triggers.schema.TriggerContextSchemaService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;

public final class EffectServiceModule implements ServiceModule {

  @Override
  public void install(ServiceAccessor services) {
    // Registries
    services.register(EffectTypeRegistryService.class, DefaultEffectTypeRegistryService.class);
    services.register(ConditionTypeRegistryService.class, DefaultConditionTypeRegistryService.class);
    services.register(AddonRegistryService.class, DefaultAddonRegistryService.class);

    // Trigger schema MUST exist before FilterService is instantiated
    services.register(TriggerContextSchemaService.class, DefaultTriggerContextSchemaService.class);

    // Filters MUST be registered before services that depend on FilterService
    services.register(FilterTypeRegistryService.class, DefaultFilterTypeRegistryService.class);
    services.register(FilterService.class, DefaultFilterService.class);

    // Action runtime + trigger pipeline
    services.register(ActionRuntimeService.class, DefaultActionRuntimeService.class);
    services.register(TriggerRuntimeService.class, DefaultTriggerRuntimeService.class);

    // Note: DefaultTriggerRegistrationService nimmt PaperPluginService im Konstruktor,
    // das bleibt DI-semantisch identisch – wir kapseln nur die Registrierung.
    services.register(TriggerRegistrationService.class, DefaultTriggerRegistrationService.class);

    services.register(TriggerBusService.class, DefaultTriggerBusService.class);
    services.register(TriggerRuleRegistryService.class, DefaultTriggerRuleRegistryService.class);

    // Engine
    services.register(LogicEngineService.class, DefaultLogicEngineService.class);
    services.register(ConditionEvaluatorService.class, DefaultConditionEvaluatorService.class);
  }
}