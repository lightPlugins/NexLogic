package io.nexstudios.nexlogic.bukkit.services.effects.loader;

import io.nexstudios.nexlogic.common.effects.config.ConfigSection;

import java.util.*;

public final class SnakeYamlConfigSection implements ConfigSection {

  private final Map<String, Object> root;

  public SnakeYamlConfigSection(Map<String, Object> root) {
    this.root = root == null ? Map.of() : root;
  }

  @Override
  public String getString(String path, String def) {
    Object v = get(path);
    return v == null ? def : String.valueOf(v);
  }

  @Override
  public int getInt(String path, int def) {
    Object v = get(path);
    if (v instanceof Number n) return n.intValue();
    try { return v == null ? def : Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return def; }
  }

  @Override
  public boolean getBoolean(String path, boolean def) {
    Object v = get(path);
    if (v instanceof Boolean b) return b;
    return v == null ? def : Boolean.parseBoolean(String.valueOf(v));
  }

  @Override
  public double getDouble(String path, double def) {
    Object v = get(path);
    if (v instanceof Number n) return n.doubleValue();
    try { return v == null ? def : Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return def; }
  }

  @Override
  public ConfigSection getSection(String path) {
    Object v = get(path);
    if (v instanceof Map<?, ?> m) {
      Map<String, Object> out = new LinkedHashMap<>();
      for (var e : m.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
      return new SnakeYamlConfigSection(out);
    }
    return null;
  }

  @Override
  public List<ConfigSection> getSectionList(String path) {
    Object v = get(path);
    if (!(v instanceof List<?> list)) return List.of();
    List<ConfigSection> out = new ArrayList<>();
    for (Object o : list) {
      if (o instanceof Map<?, ?> m) {
        Map<String, Object> mm = new LinkedHashMap<>();
        for (var e : m.entrySet()) mm.put(String.valueOf(e.getKey()), e.getValue());
        out.add(new SnakeYamlConfigSection(mm));
      } else {
        out.add(new SnakeYamlConfigSection(Map.of("value", o)));
      }
    }
    return out;
  }

  @Override
  public Set<String> getKeys(boolean deep) {
    return root.keySet();
  }

  @Override
  public Map<String, Object> getValues(boolean deep) {
    return Collections.unmodifiableMap(root);
  }

  private Object get(String path) {
    if (path == null || path.isEmpty()) return root;
    Object cur = root;
    for (String p : path.split("\\.")) {
      if (!(cur instanceof Map<?, ?> m)) return null;
      cur = m.get(p);
    }
    return cur;
  }
}