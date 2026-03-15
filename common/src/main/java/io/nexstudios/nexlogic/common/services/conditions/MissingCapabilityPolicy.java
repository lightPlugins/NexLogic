package io.nexstudios.nexlogic.common.services.conditions;

public enum MissingCapabilityPolicy {
  /**
   * If any condition requires capabilities not present in the given LogicContext,
   * evaluation stops and returns INCOMPATIBLE_CONTEXT.
   */
  FAIL_FAST,

  /**
   * Missing capability behaves like a normal failed condition.
   * (Useful if you want "best effort" checks.)
   */
  COUNT_AS_FAILED,

  /**
   * Conditions whose capabilities are missing are ignored.
   * (Useful for optional conditions across heterogeneous contexts.)
   */
  SKIP
}