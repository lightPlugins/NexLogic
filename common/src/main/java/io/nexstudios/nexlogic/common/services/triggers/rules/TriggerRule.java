package io.nexstudios.nexlogic.common.services.triggers.rules;

import io.nexstudios.nexlogic.common.model.LogicContext;

import java.util.Objects;
import java.util.function.Predicate;

public record TriggerRule(
    String owner,
    String triggerId,
    double multiplier,
    Predicate<LogicContext> predicate
) {
  public TriggerRule {
    owner = Objects.requireNonNull(owner, "owner");
    triggerId = Objects.requireNonNull(triggerId, "triggerId").toLowerCase();
    predicate = Objects.requireNonNull(predicate, "predicate");
  }

  public boolean matches(LogicContext ctx) {
    return predicate.test(ctx);
  }
}