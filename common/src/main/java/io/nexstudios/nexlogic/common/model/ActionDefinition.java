package io.nexstudios.nexlogic.common.model;

import io.nexstudios.nexlogic.common.config.ConfigSection;

public record ActionDefinition(
    String id,
    boolean enabled,
    java.util.List<String> triggers,
    java.util.List<ConfigSection> conditions,
    java.util.List<ConfigSection> effects
) {}