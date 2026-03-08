package io.nexstudios.nexlogic.common.placeholder;

public record PlaceholderKey(String identifier, String id) {

  public PlaceholderKey {
    identifier = normalize(identifier, "identifier");
    id = normalize(id, "id");
  }

  private static String normalize(String s, String name) {
    if (s == null) throw new IllegalArgumentException(name + " is null");
    String out = s.trim().toLowerCase();
    if (out.isBlank()) throw new IllegalArgumentException(name + " is blank");
    return out;
  }

  @Override
  public String toString() {
    return identifier + ":" + id;
  }

  public static PlaceholderKey of(String identifier, String id) {
    return new PlaceholderKey(identifier, id);
  }
}