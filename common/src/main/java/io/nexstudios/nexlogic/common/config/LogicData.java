package io.nexstudios.nexlogic.common.config;

import java.util.*;

/**
 * Typed access wrapper for args/config sections.
 * Keeps all parsing and defaults consistent across filters/conditions/effects.
 */
public final class LogicData {

  private final ConfigSection root;

  public LogicData(ConfigSection root) {
    this.root = root == null ? MapConfigSection.EMPTY : root;
  }

  public String getString(String path, String def) {
    return root.getString(path, def);
  }

  public String requireString(String path, String errorMessage) {
    String v = root.getString(path, null);
    if (v == null || v.isBlank()) throw new IllegalArgumentException(errorMessage);
    return v;
  }

  public boolean getBoolean(String path, boolean def) {
    return root.getBoolean(path, def);
  }

  public int getInt(String path, int def) {
    return root.getInt(path, def);
  }

  public double getDouble(String path, double def) {
    return root.getDouble(path, def);
  }

  public Set<String> getStringSet(String listPathLowercased) {
    Set<String> out = new HashSet<>();
    for (var s : root.getSectionList(listPathLowercased)) {
      String v = s.getString("value", null);
      if (v != null && !v.isBlank()) out.add(v.toLowerCase());
    }
    return out.isEmpty() ? Set.of() : Set.copyOf(out);
  }

  public Set<String> keys() {
    return root.getKeys(false);
  }

  public Map<String, Object> raw() {
    return root.getValues(true);
  }
}