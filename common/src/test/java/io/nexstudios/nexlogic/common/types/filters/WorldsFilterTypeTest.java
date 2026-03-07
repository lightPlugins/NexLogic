package io.nexstudios.nexlogic.common.types.filters;

import io.nexstudios.nexlogic.common.config.MapConfigSection;
import io.nexstudios.nexlogic.common.model.LogicContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class WorldsFilterTypeTest {

  @Test
  void returns_true_when_allow_and_deny_empty() {
    var type = new WorldsFilterType();

    var args = new MapConfigSection(Map.of(
        "allow", List.of(),
        "deny", List.of()
    ));

    var p = type.compile("break_block", args);

    var ctx = new LogicContext("break_block", null);
    assertTrue(p.test(ctx));
  }

  @Test
  void returns_false_when_world_missing_and_allow_or_deny_configured() {
    var type = new WorldsFilterType();

    var args = new MapConfigSection(Map.of(
        "allow", List.of("world")
    ));

    var p = type.compile("break_block", args);

    var ctx = new LogicContext("break_block", null);
    assertFalse(p.test(ctx));
  }

  @Test
  void deny_overrides_allow() {
    var type = new WorldsFilterType();

    var args = new MapConfigSection(Map.of(
        "allow", List.of("world", "world_nether"),
        "deny", List.of("world_nether")
    ));

    var p = type.compile("break_block", args);

    var ctx = new LogicContext("break_block", null);
    ctx.set("world", "world");
    assertTrue(p.test(ctx));

    ctx.set("world", "world_nether");
    assertFalse(p.test(ctx));
  }

  @Test
  void allow_list_is_enforced_when_present() {
    var type = new WorldsFilterType();

    var args = new MapConfigSection(Map.of(
        "allow", List.of("world")
    ));

    var p = type.compile("break_block", args);

    var ctx = new LogicContext("break_block", null);
    ctx.set("world", "world");
    assertTrue(p.test(ctx));

    ctx.set("world", "world_the_end");
    assertFalse(p.test(ctx));
  }

  @Test
  void deny_list_blocks_even_without_allow() {
    var type = new WorldsFilterType();

    var args = new MapConfigSection(Map.of(
        "deny", List.of("world_the_end")
    ));

    var p = type.compile("break_block", args);

    var ctx = new LogicContext("break_block", null);
    ctx.set("world", "world");
    assertTrue(p.test(ctx));

    ctx.set("world", "world_the_end");
    assertFalse(p.test(ctx));
  }
}
