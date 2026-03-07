package io.nexstudios.nexlogic.common.services.triggers.rules;

import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.serviceregistry.di.Service;

import java.util.List;

public interface TriggerRuleRegistryService extends Service {
  void register(TriggerRule rule);
  void unregisterOwner(String owner);

  List<TriggerRule> match(String triggerId, LogicContext ctx);

  default double totalXpFromMatches(String triggerId, LogicContext ctx, double baseXp) {
    double total = 0.0;
    for (var r : match(triggerId, ctx)) {
      total += baseXp * r.multiplier();
    }
    return total;
  }

  /**
   * Registers a rule from a config section like:
   * - trigger: break_block
   *   multiplier: 0.5
   *   filters:
   *     blocks: [stone]
   */
  void registerFromConfig(String owner, ConfigSection ruleSection);
}