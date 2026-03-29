package io.nexstudios.nexlogic.common.services.triggers.register;

import io.nexstudios.nexlogic.common.effects.model.CompiledAction;
import io.nexstudios.serviceregistry.di.Service;

import java.util.List;
import java.util.Map;

/**
 * Allows other plugins to register compiled actions per trigger (owner-scoped).
 * The TriggerBus will execute these actions in addition to the base "swap()" map.
 */
public interface TriggerRegistrationService extends Service {

  void registerOwner(String owner, Map<String, List<CompiledAction>> compiledByTrigger);

  void unregisterOwner(String owner);

  List<CompiledAction> combine(String triggerIdLower, List<CompiledAction> base);

  Map<String, List<CompiledAction>> getAllByTrigger();

  Map<String, List<CompiledAction>> getInternalByTrigger();
}