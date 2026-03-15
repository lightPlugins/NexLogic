package io.nexstudios.nexlogic.common.services.conditions;

public enum ConditionOutcome {
  EMPTY,
  ALL_SUCCESS,
  AT_LEAST_ONE_SUCCESS,
  ALL_FAILED,
  PARTIAL,
  INCOMPATIBLE_CONTEXT,
  ERROR;

  public boolean successFor(ConditionAggregationMode mode) {
    if (mode == null) mode = ConditionAggregationMode.ALL;
    return switch (mode) {
      case ALL -> this == ALL_SUCCESS;
      case ANY -> this == AT_LEAST_ONE_SUCCESS;
    };
  }
}