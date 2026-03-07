package io.nexstudios.nexlogic.common.runtime;

import io.nexstudios.nexlogic.common.model.LogicContext;

@FunctionalInterface
public interface EffectInstance {
  void run(LogicContext ctx);
}