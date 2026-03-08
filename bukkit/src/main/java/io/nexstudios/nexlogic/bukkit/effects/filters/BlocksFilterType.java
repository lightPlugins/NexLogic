package io.nexstudios.nexlogic.bukkit.effects.filters;

import io.nexstudios.nexlogic.bukkit.services.effects.blocks.BlockKeyService;
import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextResolverService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.PlaceholderRuntimeService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.config.LogicData;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.effects.types.FilterTypeService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.block.Block;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Dependencies({
    BukkitContextResolverService.class,
    BlockKeyService.class,
    PlaceholderRuntimeService.class
})
public final class BlocksFilterType implements FilterTypeService {

  private final BukkitContextResolverService resolver;
  private final BlockKeyService blockKeys;
  private final PlaceholderRuntimeService placeholders;

  public BlocksFilterType(ServiceAccessor service) {
    this.resolver = service.getService(BukkitContextResolverService.class);
    this.blockKeys = service.getService(BlockKeyService.class);
    this.placeholders = service.getService(PlaceholderRuntimeService.class);
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
    return ctx -> {
      ConfigSection resolvedArgs = placeholders.resolveSection(args, ctx);
      LogicData data = new LogicData(resolvedArgs);

      Set<String> blocksRaw = data.getStringSet("blocks");
      boolean inverted = data.getBoolean("inverted", false);

      if (blocksRaw.isEmpty()) return true;

      Set<String> allowed = blocksRaw.stream()
          .map(blockKeys::normalize)
          .filter(s -> !s.isBlank())
          .collect(Collectors.toUnmodifiableSet());

      Block block = resolver.block(ctx).orElse(null);
      if (block == null) return false;

      boolean match = blockKeys.matches(block, allowed);
      return inverted != match;
    };
  }
}