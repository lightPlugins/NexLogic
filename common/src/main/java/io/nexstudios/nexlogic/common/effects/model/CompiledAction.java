package io.nexstudios.nexlogic.common.effects.model;

import io.nexstudios.nexlogic.common.effects.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.effects.runtime.EffectInstance;

public record CompiledAction(
    String id,
    java.util.List<String> triggers,
    java.util.List<ConditionInstance> conditions,
    java.util.List<EffectInstance> effects
) {}