package io.nexstudios.nexlogic.common.services.triggers.register;

import io.nexstudios.nexlogic.common.effects.model.CompiledAction;
import io.nexstudios.framework.paper.services.plugin.PaperPluginService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultTriggerRegistrationService implements TriggerRegistrationService {

  private final ConcurrentHashMap<String, Map<String, List<CompiledAction>>> byOwner = new ConcurrentHashMap<>();

  public DefaultTriggerRegistrationService(PaperPluginService core) {
    // no-op, but keeps construction consistent with other services
  }

  @Override
  public void registerOwner(String owner, Map<String, List<CompiledAction>> compiledByTrigger) {
    if (owner == null || owner.isBlank()) throw new IllegalArgumentException("owner is required");
    Objects.requireNonNull(compiledByTrigger, "compiledByTrigger");

    // Normalize trigger keys to lowercase & freeze lists
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
}