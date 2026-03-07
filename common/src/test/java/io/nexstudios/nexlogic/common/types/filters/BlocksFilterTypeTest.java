package io.nexstudios.nexlogic.common.types.filters;

import io.nexstudios.nexlogic.common.config.MapConfigSection;
import io.nexstudios.nexlogic.common.model.LogicContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class BlocksFilterTypeTest {

  @Test
  void returns_true_when_blocks_list_is_empty() {
    var type = new BlocksFilterType();

    var args = new MapConfigSection(Map.of(
        "blocks", List.of()
    ));

    var p = type.compile("break_block", args);

    var ctx = new LogicContext("break_block", null);
    assertTrue(p.test(ctx));
  }

  @Test
  void returns_false_when_block_type_missing_and_blocks_configured() {
    var type = new BlocksFilterType();

    var args = new MapConfigSection(Map.of(
        "blocks", List.of("stone")
    ));

    var p = type.compile("break_block", args);

    var ctx = new LogicContext("break_block", null);
    assertFalse(p.test(ctx));
  }

  @Test
  void matches_block_type_case_insensitive() {
    var type = new BlocksFilterType();

    var args = new MapConfigSection(Map.of(
        "blocks", List.of("Stone", "NETHERRACK")
    ));

    var p = type.compile("break_block", args);

    var ctx = new LogicContext("break_block", null);
    ctx.set("block.type", "stone");
    assertTrue(p.test(ctx));

    ctx.set("block.type", "NETHERRACK");
    assertTrue(p.test(ctx));

    ctx.set("block.type", "dirt");
    assertFalse(p.test(ctx));
  }

  @Test
  void inverted_flips_result() {
    var type = new BlocksFilterType();

    var args = new MapConfigSection(Map.of(
        "blocks", List.of("stone"),
        "inverted", true
    ));

    var p = type.compile("break_block", args);

    var ctx = new LogicContext("break_block", null);
    ctx.set("block.type", "stone");
    assertFalse(p.test(ctx));

    ctx.set("block.type", "dirt");
    assertTrue(p.test(ctx));
  }
}