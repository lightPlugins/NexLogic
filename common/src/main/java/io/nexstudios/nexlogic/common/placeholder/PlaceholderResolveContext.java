package io.nexstudios.nexlogic.common.placeholder;

import io.nexstudios.nexlogic.common.effects.model.LogicContext;

import java.util.Map;

public record PlaceholderResolveContext(
    LogicContext logicContext,
    String defaultIdentifier,
    String cacheScopeKey,
    Map<String, Object> variables
) {

  public PlaceholderResolveContext {
    defaultIdentifier = defaultIdentifier == null ? "" : defaultIdentifier.trim().toLowerCase();
    cacheScopeKey = cacheScopeKey == null ? "global" : cacheScopeKey;
    variables = variables == null ? Map.of() : Map.copyOf(variables);
  }

  public static PlaceholderResolveContext of(LogicContext ctx, String defaultIdentifier) {
    return new PlaceholderResolveContext(ctx, defaultIdentifier, "global", Map.of());
  }

  public PlaceholderResolveContext withCacheScopeKey(String key) {
    return new PlaceholderResolveContext(logicContext, defaultIdentifier, key, variables);
  }

  public PlaceholderResolveContext withVariables(Map<String, Object> vars) {
    return new PlaceholderResolveContext(logicContext, defaultIdentifier, cacheScopeKey, vars);
  }
}