package io.nexstudios.nexlogic.common.types.filters;

import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.nexlogic.common.services.types.filter.FilterTypeService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public final class WorldsFilterType implements FilterTypeService {

  @Override
  public String id() {
    return "worlds";
  }

  @Override
  public Set<ContextCapability> requiredCapabilities() {
    return Set.of(ContextCapability.WORLD);
  }

  @Override
  public Predicate<LogicContext> compile(String triggerId, ConfigSection args) {
    Set<String> allow = readStringSet(args, "allow");
    Set<String> deny = readStringSet(args, "deny");

    if (allow.isEmpty() && deny.isEmpty()) return ctx -> true;

    return ctx -> {
      Object v = ctx.get("world");
      if (v == null) return false;
      String world = String.valueOf(v).toLowerCase();

      if (!deny.isEmpty() && deny.contains(world)) return false;
      if (!allow.isEmpty()) return allow.contains(world);
      return true;
    };
  }

  private static Set<String> readStringSet(ConfigSection root, String listPath) {
    if (root == null) return Set.of();
    Set<String> out = new HashSet<>();
    for (var s : root.getSectionList(listPath)) {
      String v = s.getString("value", null);
      if (v != null && !v.isBlank()) out.add(v.toLowerCase());
    }
    return out;
  }
}