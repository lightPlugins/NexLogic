package io.nexstudios.nexlogic.common.model;

import io.nexstudios.nexlogic.common.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.runtime.EffectInstance;

public record CompiledAction(
    String id,
    java.util.List<String> triggers,
    java.util.List<ConditionInstance> conditions,
    java.util.List<EffectInstance> effects
) {}