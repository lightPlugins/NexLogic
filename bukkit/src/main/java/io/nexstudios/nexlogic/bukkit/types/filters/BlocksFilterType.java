package io.nexstudios.nexlogic.bukkit.types.filters;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.bukkit.services.context.BukkitContextResolverService;
import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.config.LogicData;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.nexlogic.common.types.FilterTypeService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import org.bukkit.block.Block;

import java.util.Set;
import java.util.function.Predicate;

public final class BlocksFilterType implements FilterTypeService {

  private final BukkitContextResolverService resolver;

  public BlocksFilterType(PaperPluginService core) {
    this.resolver = core.plugin().services().getService(BukkitContextResolverService.class);
  }

  @Override
  public String id() {
    return "blocks";
  }

  @Override
  public Set<ContextCapability> requiredCapabilities() {
    return Set.of(ContextCapability.BLOCK);
  }

  @Override
  public Predicate<LogicContext> compile(String triggerId, ConfigSection args) {
    LogicData data = new LogicData(args);
    Set<String> blocks = data.getStringSet("blocks");
    boolean inverted = data.getBoolean("inverted", false);

    if (blocks.isEmpty()) return ctx -> true;

    return ctx -> {
      Block block = resolver.block(ctx).orElse(null);
      if (block == null) return false;

      String typeLower = block.getType().name().toLowerCase();
      boolean match = blocks.contains(typeLower);
      return inverted != match;
    };
  }
}