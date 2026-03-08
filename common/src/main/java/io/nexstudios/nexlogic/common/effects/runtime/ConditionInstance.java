package io.nexstudios.nexlogic.common.effects.runtime;

import io.nexstudios.nexlogic.common.effects.model.LogicContext;

@FunctionalInterface
public interface ConditionInstance {
  boolean test(LogicContext ctx);
}