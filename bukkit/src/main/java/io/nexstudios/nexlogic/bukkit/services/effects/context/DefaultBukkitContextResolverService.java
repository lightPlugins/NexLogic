package io.nexstudios.nexlogic.bukkit.services.effects.context;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class DefaultBukkitContextResolverService implements BukkitContextResolverService {

  public DefaultBukkitContextResolverService(PaperPluginService core) {
    // no-op
  }

  @Override
  public Optional<Player> player(LogicContext ctx) {
    if (ctx == null) return Optional.empty();
    return ctx.get(BukkitContextKeys.PLAYER);
  }

  @Override
  public Optional<World> world(LogicContext ctx) {
    if (ctx == null) return Optional.empty();
    return ctx.get(BukkitContextKeys.WORLD);
  }

  @Override
  public Optional<Location> location(LogicContext ctx) {
    if (ctx == null) return Optional.empty();
    return ctx.get(BukkitContextKeys.LOCATION);
  }

  @Override
  public Optional<Block> block(LogicContext ctx) {
    if (ctx == null) return Optional.empty();
    return ctx.get(BukkitContextKeys.BLOCK);
  }

  @Override
  public Optional<ItemStack> item(LogicContext ctx) {
    if (ctx == null) return Optional.empty();
    return ctx.get(BukkitContextKeys.ITEM_STACK);
  }

  @Override
  public Optional<Entity> entity(LogicContext ctx) {
    if (ctx == null) return Optional.empty();
    return ctx.get(BukkitContextKeys.ENTITY);
  }


}