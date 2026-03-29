package io.nexstudios.nexlogic.common.services.triggers.register;

import io.nexstudios.nexlogic.common.effects.model.CompiledAction;
import io.nexstudios.framework.paper.services.plugin.PaperPluginService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultTriggerRegistrationService implements TriggerRegistrationService {

  private final ConcurrentHashMap<String, Map<String, List<CompiledAction>>> byOwner = new ConcurrentHashMap<>();
  private volatile String internalOwner = null;

  public DefaultTriggerRegistrationService(PaperPluginService core) {
  }

  public void setInternalOwner(String owner) {
    this.internalOwner = owner == null ? null : owner.toLowerCase();
  }

  @Override
  public void registerOwner(String owner, Map<String, List<CompiledAction>> compiledByTrigger) {
    if (owner == null || owner.isBlank()) throw new IllegalArgumentException("owner is required");
    Objects.requireNonNull(compiledByTrigger, "compiledByTrigger");

    Map<String, List<CompiledAction>> normalized = new HashMap<>();
    for (var e : compiledByTrigger.entrySet()) {
      if (e.getKey() == null) continue;
      String t = e.getKey().toLowerCase();
      List<CompiledAction> list = e.getValue() == null ? List.of() : List.copyOf(e.getValue());
      if (!list.isEmpty()) normalized.put(t, list);
    }

    byOwner.put(owner.toLowerCase(), Map.copyOf(normalized));
  }

  @Override
  public void unregisterOwner(String owner) {
    if (owner == null || owner.isBlank()) return;
    byOwner.remove(owner.toLowerCase());
  }

  @Override
  public List<CompiledAction> combine(String triggerIdLower, List<CompiledAction> base) {
    if (triggerIdLower == null) return base == null ? List.of() : base;
    List<CompiledAction> out = new ArrayList<>(base == null ? List.of() : base);

    for (var ownerEntry : byOwner.values()) {
      var extra = ownerEntry.get(triggerIdLower);
      if (extra != null && !extra.isEmpty()) out.addAll(extra);
    }

    return out.isEmpty() ? List.of() : List.copyOf(out);
  }

  public Map<String, List<CompiledAction>> getAllByTrigger() {
    Map<String, List<CompiledAction>> result = new HashMap<>();
    for (var ownerEntry : byOwner.values()) {
      for (var e : ownerEntry.entrySet()) {
        String trigger = e.getKey();
        List<CompiledAction> actions = e.getValue();
        result.computeIfAbsent(trigger, k -> new ArrayList<>()).addAll(actions);
      }
    }
    for (var e : result.entrySet()) {
      e.setValue(List.copyOf(e.getValue()));
    }
    return Map.copyOf(result);
  }

  public Map<String, List<CompiledAction>> getInternalByTrigger() {
    if (internalOwner == null) return Map.of();
    var internal = byOwner.get(internalOwner);
    if (internal == null) return Map.of();
    return Map.copyOf(internal);
  }
}
