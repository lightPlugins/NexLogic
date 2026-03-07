package io.nexstudios.nexlogic.common.model;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create one LogicContext per trigger invocation.
 * Variables map is thread-safe; effect/condition implementations may not be.
 */
public final class LogicContext {
  private final String triggerId;
  private final String actorUuid;
  private final Map<String, Object> vars = new ConcurrentHashMap<>();

  public LogicContext(String triggerId, String actorUuid) {
    this.triggerId = Objects.requireNonNull(triggerId, "triggerId");
    this.actorUuid = actorUuid;
  }

  public String triggerId() {
    return triggerId;
  }

  public Optional<String> actorUuid() {
    return Optional.ofNullable(actorUuid);
  }

  public Object get(String key) {
    return vars.get(key);
  }

  public void set(String key, Object value) {
    vars.put(key, value);
  }

  public Map<String, Object> vars() {
    return vars;
  }
}