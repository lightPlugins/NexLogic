package io.nexstudios.nexlogic.common.services.triggers.register;

import io.nexstudios.nexlogic.common.model.CompiledAction;
import io.nexstudios.nexlogic.common.runtime.EffectInstance;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class DefaultTriggerRegistrationServiceTest {

  private static CompiledAction action(String id) {
    EffectInstance noop = ctx -> {};
    return new CompiledAction(id, List.of("join"), List.of(ctx -> true), List.of(noop));
  }

  @Test
  void registerOwner_normalizes_trigger_keys_and_combines() {
    var reg = new DefaultTriggerRegistrationService(null);

    var a1 = action("a1");

    reg.registerOwner("OwnerA", Map.of(
        "JOIN", List.of(a1),
        "break_block", List.of()
    ));

    var combined = reg.combine("join", List.of());
    assertEquals(1, combined.size());
    assertEquals("a1", combined.getFirst().id());
  }

  @Test
  void registerOwner_when_keys_collide_after_lowercasing_last_wins() {
    var reg = new DefaultTriggerRegistrationService(null);

    var a1 = action("a1");
    var a2 = action("a2");

    Map<String, List<CompiledAction>> input = new LinkedHashMap<>();
    input.put("JOIN", List.of(a1));
    input.put("join", List.of(a2));

    reg.registerOwner("OwnerA", input);

    var combined = reg.combine("join", List.of());
    assertEquals(1, combined.size());
    assertEquals("a2", combined.getFirst().id());
  }

  @Test
  void unregisterOwner_removes_actions_from_combine() {
    var reg = new DefaultTriggerRegistrationService(null);

    reg.registerOwner("OwnerA", Map.of("join", List.of(action("a1"))));
    reg.registerOwner("OwnerB", Map.of("join", List.of(action("b1"))));

    assertEquals(2, reg.combine("join", List.of()).size());

    reg.unregisterOwner("OwnerA");
    var combined = reg.combine("join", List.of());
    assertEquals(1, combined.size());
    assertEquals("b1", combined.getFirst().id());
  }

  @Test
  void combine_keeps_base_and_appends_owner_actions() {
    var reg = new DefaultTriggerRegistrationService(null);

    var base = List.of(action("base1"));
    reg.registerOwner("OwnerA", Map.of("join", List.of(action("a1"))));

    var combined = reg.combine("join", base);
    assertEquals(2, combined.size());
    assertEquals("base1", combined.getFirst().id());
  }
}