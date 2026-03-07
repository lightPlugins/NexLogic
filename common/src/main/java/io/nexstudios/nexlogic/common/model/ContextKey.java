package io.nexstudios.nexlogic.common.model;

import java.util.Objects;

/**
 * Typed key for storing runtime objects inside LogicContext.
 * This allows platform modules (e.g. Bukkit) to attach native objects without making common depend on them.
 */
public final class ContextKey<T> {

  private final String id;
  private final Class<T> type;

  public ContextKey(String id, Class<T> type) {
    this.id = Objects.requireNonNull(id, "id");
    this.type = Objects.requireNonNull(type, "type");
  }

  public String id() {
    return id;
  }

  public Class<T> type() {
    return type;
  }

  @Override
  public String toString() {
    return "ContextKey(" + id + ":" + type.getSimpleName() + ")";
  }
}