package io.nexstudios.nexlogic.common.services.engine;

import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.serviceregistry.di.Service;

import java.util.List;

/**
 * Public entry point for other plugins.
 * Contract: Types are identified by "id" and take their parameters only from "args".
 */
public interface LogicEngineService extends Service {

  boolean testConditions(List<ConfigSection> conditions, LogicContext ctx);

  void executeEffects(List<ConfigSection> effects, LogicContext ctx);

  boolean fireEffectStyle(String triggerId, LogicContext ctx, List<ConfigSection> effectEntries);

  boolean fireTriggerStyle(String triggerId, LogicContext ctx, List<ConfigSection> triggerEntries);

  void registerEffectStyle(String owner, List<ConfigSection> effectEntries);

  void registerTriggerStyle(String owner, List<ConfigSection> triggerEntries);

  void unregisterOwner(String owner);

  // --- Convenience overloads (create minimal context internally) ---
  default boolean testConditions(List<ConfigSection> conditions) {
    return testConditions(conditions, new LogicContext("conditions_only", null));
  }

  default void executeEffects(List<ConfigSection> effects) {
    executeEffects(effects, new LogicContext("effects_only", null));
  }

  default boolean fireEffectStyle(String triggerId, List<ConfigSection> effectEntries) {
    return fireEffectStyle(triggerId, new LogicContext(triggerId, null), effectEntries);
  }

  default boolean fireTriggerStyle(String triggerId, List<ConfigSection> triggerEntries) {
    return fireTriggerStyle(triggerId, new LogicContext(triggerId, null), triggerEntries);
  }

}