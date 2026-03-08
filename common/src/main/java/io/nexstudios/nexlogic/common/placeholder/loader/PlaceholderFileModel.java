package io.nexstudios.nexlogic.common.placeholder.loader;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public record PlaceholderFileModel(
    String owner,
    String identifier,
    boolean enabled,
    Duration defaultTtl,
    List<Entry> placeholders
) {
  public record Entry(String id, String value, Duration ttlOverride) {}

  public Map<String, Object> debug() {
    return Map.of(
        "owner", owner,
        "identifier", identifier,
        "enabled", enabled,
        "defaultTtl", defaultTtl.toString(),
        "count", placeholders == null ? 0 : placeholders.size()
    );
  }
}