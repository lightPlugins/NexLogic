package io.nexstudios.nexlogic.common.services.conditions;

import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record ConditionEvaluationResult(
    ConditionOutcome outcome,
    int total,
    int passed,
    int failed,
    Set<ContextCapability> missingCapabilities,
    List<String> errors,
    Map<String, Boolean> perCondition
) {

  public ConditionEvaluationResult {
    outcome = outcome == null ? ConditionOutcome.ERROR : outcome;
    missingCapabilities = missingCapabilities == null ? Set.of() : Set.copyOf(missingCapabilities);
    errors = errors == null ? List.of() : List.copyOf(errors);
    perCondition = perCondition == null ? Map.of() : Map.copyOf(perCondition);
  }

  public boolean successFor(ConditionAggregationMode mode) {
    return outcome.successFor(mode);
  }
}