package io.nexstudios.nexlogic.common.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class MapConfigSectionTest {

  @Test
  void get_primitives_and_defaults() {
    var cfg = new MapConfigSection(Map.of(
        "a", "hello",
        "b", 123,
        "c", "true",
        "d", 1.5
    ));

    assertEquals("hello", cfg.getString("a", "x"));
    assertEquals(123, cfg.getInt("b", 0));
    assertTrue(cfg.getBoolean("c", false));
    assertEquals(1.5, cfg.getDouble("d", 0.0), 0.000001);

    assertEquals("def", cfg.getString("missing", "def"));
    assertEquals(7, cfg.getInt("missing", 7));
    assertFalse(cfg.getBoolean("missing", false));
    assertEquals(2.0, cfg.getDouble("missing", 2.0), 0.000001);
  }

  @Test
  void get_nested_paths_and_sections() {
    var cfg = new MapConfigSection(Map.of(
        "root", Map.of(
            "child", Map.of(
                "value", "ok"
            )
        )
    ));

    assertEquals("ok", cfg.getString("root.child.value", "nope"));

    ConfigSection child = cfg.getSection("root.child");
    assertNotNull(child);
    assertEquals("ok", child.getString("value", "nope"));
  }

  @Test
  void get_section_list_wraps_non_maps_as_value_entries() {
    var cfg = new MapConfigSection(Map.of(
        "list", List.of(
            Map.of("id", "a"),
            "plain",
            5
        )
    ));

    var sections = cfg.getSectionList("list");
    assertEquals(3, sections.size());

    assertEquals("a", sections.get(0).getString("id", null));
    assertEquals("plain", sections.get(1).getString("value", null));
    assertEquals("5", sections.get(2).getString("value", null));
  }
}
