package io.nexstudios.nexlogic.common.services.triggers.rules;

import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.nexlogic.common.services.filters.FilterService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Dependencies({
    FilterService.class
})
public final class DefaultTriggerRuleRegistryService implements TriggerRuleRegistryService {

  private final FilterService filters;
  private final ConcurrentHashMap<String, CopyOnWriteArrayList<TriggerRule>> byTrigger = new ConcurrentHashMap<>();

  public DefaultTriggerRuleRegistryService(ServiceAccessor accessor) {
    this.filters = accessor.getService(FilterService.class);
  }

  @Override
  public void register(TriggerRule rule) {
    byTrigger.computeIfAbsent(rule.triggerId(), k -> new CopyOnWriteArrayList<>()).add(rule);
  }

  @Override
  public void unregisterOwner(String owner) {
    if (owner == null) return;
    for (var entry : byTrigger.entrySet()) {
      entry.getValue().removeIf(r -> owner.equalsIgnoreCase(r.owner()));
      if (entry.getValue().isEmpty()) {
        byTrigger.remove(entry.getKey(), entry.getValue());
      }
    }
  }

  @Override
  public List<TriggerRule> match(String triggerId, LogicContext ctx) {
    if (triggerId == null) return List.of();
    var list = byTrigger.get(triggerId.toLowerCase());
    if (list == null || list.isEmpty()) return List.of();

    List<TriggerRule> out = new ArrayList<>();
    for (var r : list) {
      if (r.matches(ctx)) out.add(r);
    }
    return out;
  }

  @Override
  public void registerFromConfig(String owner, ConfigSection ruleSection) {
    if (ruleSection == null) throw new IllegalArgumentException("ruleSection is null");

    String triggerId = ruleSection.getString("trigger", null);
    if (triggerId == null) throw new IllegalArgumentException("ruleSection missing 'trigger'");

    double multiplier = ruleSection.getDouble("multiplier", 1.0);
    ConfigSection fs = ruleSection.getSection("filters");

    var predicate = filters.compile(triggerId, fs);
    register(new TriggerRule(owner, triggerId, multiplier, predicate));
  }
}