package io.nexstudios.nexlogic.common.types.filters;

import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.nexlogic.common.services.types.filter.FilterTypeService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public final class BlocksFilterType implements FilterTypeService {

  @Override
  public String id() {
    return "blocks";
  }

  @Override
  public Set<ContextCapability> requiredCapabilities() {
    return Set.of(ContextCapability.BLOCK, ContextCapability.PLAYER, ContextCapability.WORLD);
  }

  @Override
  public Predicate<LogicContext> compile(String triggerId, ConfigSection args) {
    Set<String> blocks = readStringSet(args, "blocks");
    boolean inverted = args != null && args.getBoolean("inverted", false);

    if (blocks.isEmpty()) return ctx -> true;

    return ctx -> {
      Object v = ctx.get("block.type");
      if (v == null) return false;
      boolean match = blocks.contains(String.valueOf(v).toLowerCase());
      return inverted != match;
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