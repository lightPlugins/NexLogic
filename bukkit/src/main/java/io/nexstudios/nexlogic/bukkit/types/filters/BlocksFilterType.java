package io.nexstudios.nexlogic.bukkit.types.filters;

import io.nexstudios.nexlogic.bukkit.services.blocks.BlockKeyService;
import io.nexstudios.nexlogic.bukkit.services.context.BukkitContextResolverService;
import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.config.LogicData;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.nexlogic.common.types.FilterTypeService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.block.Block;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Dependencies({
    BukkitContextResolverService.class,
    BlockKeyService.class
})
public final class BlocksFilterType implements FilterTypeService {

  private final BukkitContextResolverService resolver;
  private final BlockKeyService blockKeys;

  public BlocksFilterType(ServiceAccessor service) {
    this.resolver = service.getService(BukkitContextResolverService.class);
    this.blockKeys = service.getService(BlockKeyService.class);
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
    Set<String> blocksRaw = data.getStringSet("blocks");
    boolean inverted = data.getBoolean("inverted", false);

    if (blocksRaw.isEmpty()) return ctx -> true;

    Set<String> allowed = blocksRaw.stream()
        .map(blockKeys::normalize)
        .filter(s -> !s.isBlank())
        .collect(Collectors.toUnmodifiableSet());

    return ctx -> {
      Block block = resolver.block(ctx).orElse(null);
      if (block == null) return false;

      boolean match = blockKeys.matches(block, allowed);
      return inverted != match;
    };
  }
}