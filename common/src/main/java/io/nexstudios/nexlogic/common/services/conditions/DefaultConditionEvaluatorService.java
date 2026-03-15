package io.nexstudios.nexlogic.common.services.conditions;

import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.effects.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.nexlogic.common.services.registry.condition.ConditionTypeRegistryService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Dependencies({
    ConditionTypeRegistryService.class,
    LoggerService.class
})
public final class DefaultConditionEvaluatorService implements ConditionEvaluatorService {

  private final ConditionTypeRegistryService registry;
  private final LoggerService logger;

  public DefaultConditionEvaluatorService(ServiceAccessor services) {
    this.registry = services.getService(ConditionTypeRegistryService.class);
    this.logger = services.getService(LoggerService.class);
  }

  @Override
  public ConditionEvaluationResult evaluate(
      List<ConfigSection> conditionEntries,
      LogicContext ctx,
      ConditionAggregationMode mode,
      MissingCapabilityPolicy missingCapabilityPolicy
  ) {
    ConditionEvaluatorService.requireContext(ctx);
    if (mode == null) mode = ConditionAggregationMode.ALL;
    if (missingCapabilityPolicy == null) missingCapabilityPolicy = MissingCapabilityPolicy.FAIL_FAST;

    if (conditionEntries == null || conditionEntries.isEmpty()) {
      return new ConditionEvaluationResult(
          ConditionOutcome.EMPTY,
          0, 0, 0,
          Set.of(),
          List.of(),
          Map.of()
      );
    }

    Set<ContextCapability> provided = ctx.capabilities();

    int total = 0;
    int passed = 0;
    int failed = 0;

    EnumSet<ContextCapability> missingCaps = EnumSet.noneOf(ContextCapability.class);
    List<String> errors = new ArrayList<>();
    Map<String, Boolean> perCondition = new LinkedHashMap<>();

    boolean anySuccess = false;
    boolean anyFailure = false;

    for (int i = 0; i < conditionEntries.size(); i++) {
      ConfigSection entry = conditionEntries.get(i);
      if (entry == null) continue;

      String id = entry.getString("id", null);
      if (id == null || id.isBlank()) {
        errors.add("Condition entry missing 'id' at index " + i);
        return new ConditionEvaluationResult(
            ConditionOutcome.ERROR,
            total, passed, failed,
            Set.copyOf(missingCaps),
            List.copyOf(errors),
            Map.copyOf(perCondition)
        );
      }

      total++;

      ConditionInstance inst;
      Set<ContextCapability> required;
      try {
        var type = registry.resolve(id).orElseThrow(() ->
            new IllegalArgumentException("Unknown condition id '" + id + "'")
        );
        required = type.requiredCapabilities();
        if (required == null) required = Set.of();

        if (!provided.containsAll(required)) {
          EnumSet<ContextCapability> missing = required.isEmpty()
              ? EnumSet.noneOf(ContextCapability.class)
              : EnumSet.copyOf(required);

          missing.removeAll(provided);
          missingCaps.addAll(missing);

          switch (missingCapabilityPolicy) {
            case FAIL_FAST -> {
              return new ConditionEvaluationResult(
                  ConditionOutcome.INCOMPATIBLE_CONTEXT,
                  total, passed, failed + 1,
                  Set.copyOf(missingCaps),
                  List.copyOf(errors),
                  Map.copyOf(perCondition)
              );
            }
            case COUNT_AS_FAILED -> {
              failed++;
              anyFailure = true;
              perCondition.put(id, false);
              continue;
            }
            case SKIP -> {
              perCondition.put(id, false);
              total--;
              continue;
            }
          }
        }

        inst = type.create(entry);
      } catch (Throwable t) {
        logger.logger().severe("Failed to compile condition '" + id + "': " + t.getMessage());
        errors.add("compile:" + id + ":" + t.getMessage());

        return new ConditionEvaluationResult(
            ConditionOutcome.ERROR,
            total, passed, failed,
            Set.copyOf(missingCaps),
            List.copyOf(errors),
            Map.copyOf(perCondition)
        );
      }

      boolean ok;
      try {
        ok = inst.test(ctx);
      } catch (Throwable t) {
        logger.logger().severe("Condition '" + id + "' threw during evaluation: " + t.getMessage());
        errors.add("eval:" + id + ":" + t.getMessage());
        return new ConditionEvaluationResult(
            ConditionOutcome.ERROR,
            total, passed, failed,
            Set.copyOf(missingCaps),
            List.copyOf(errors),
            Map.copyOf(perCondition)
        );
      }

      perCondition.put(id, ok);

      if (ok) {
        passed++;
        anySuccess = true;
        if (mode == ConditionAggregationMode.ANY) {
          return new ConditionEvaluationResult(
              ConditionOutcome.AT_LEAST_ONE_SUCCESS,
              total, passed, failed,
              Set.copyOf(missingCaps),
              List.copyOf(errors),
              Map.copyOf(perCondition)
          );
        }
      } else {
        failed++;
        anyFailure = true;
        if (mode == ConditionAggregationMode.ALL) {
          return new ConditionEvaluationResult(
              ConditionOutcome.ALL_FAILED,
              total, passed, failed,
              Set.copyOf(missingCaps),
              List.copyOf(errors),
              Map.copyOf(perCondition)
          );
        }
      }
    }

    ConditionOutcome outcome;
    if (total <= 0) {
      outcome = ConditionOutcome.EMPTY;
    } else if (!anyFailure && anySuccess) {
      outcome = ConditionOutcome.ALL_SUCCESS;
    } else if (anySuccess) {
      outcome = ConditionOutcome.PARTIAL;
    } else {
      outcome = ConditionOutcome.ALL_FAILED;
    }

    return new ConditionEvaluationResult(
        outcome,
        total, passed, failed,
        Set.copyOf(missingCaps),
        List.copyOf(errors),
        Map.copyOf(perCondition)
    );
  }
}