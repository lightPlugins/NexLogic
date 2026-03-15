package io.nexstudios.nexlogic.common.services.conditions;

import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.serviceregistry.di.Service;

import java.util.List;
import java.util.Objects;

public interface ConditionEvaluatorService extends Service {

  ConditionEvaluationResult evaluate(
      List<ConfigSection> conditionEntries,
      LogicContext ctx,
      ConditionAggregationMode mode,
      MissingCapabilityPolicy missingCapabilityPolicy
  );

  default ConditionEvaluationResult evaluate(List<ConfigSection> conditionEntries, LogicContext ctx) {
    return evaluate(conditionEntries, ctx, ConditionAggregationMode.ALL, MissingCapabilityPolicy.FAIL_FAST);
  }


  /**
   * Evaluates a set of conditions defined in a configuration section.
   *
   * @param conditionsOwner the configuration section containing the conditions to evaluate; if null, an empty list of conditions will be evaluated
   * @param listPath the path within the configuration section for the list of condition entries; if null, an empty string will be used as the path
   * @param ctx the logic context providing contextual information for the evaluation
   * @param mode the aggregation mode specifying how conditions are combined (e.g., ALL or ANY)
   * @param missingCapabilityPolicy the policy to handle missing capabilities during the evaluation
   * @return the result of the condition evaluation, including the outcome, counts of conditions passed and failed, missing capabilities, and errors if any
   */
  default ConditionEvaluationResult evaluateAt(
      ConfigSection conditionsOwner,
      String listPath,
      LogicContext ctx,
      ConditionAggregationMode mode,
      MissingCapabilityPolicy missingCapabilityPolicy
  ) {
    if (conditionsOwner == null) {
      return evaluate(List.of(), ctx, mode, missingCapabilityPolicy);
    }
    String p = (listPath == null) ? "" : listPath;
    return evaluate(conditionsOwner.getSectionList(p), ctx, mode, missingCapabilityPolicy);
  }

  default ConditionEvaluationResult evaluateAt(ConfigSection conditionsOwner, String listPath, LogicContext ctx) {
    return evaluateAt(conditionsOwner, listPath, ctx, ConditionAggregationMode.ALL, MissingCapabilityPolicy.FAIL_FAST);
  }

  static void requireContext(LogicContext ctx) {
    Objects.requireNonNull(ctx, "ctx");
  }
}