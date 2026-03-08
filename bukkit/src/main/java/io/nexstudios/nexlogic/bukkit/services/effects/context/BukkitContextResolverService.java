package io.nexstudios.nexlogic.bukkit.services.effects.context;

import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.serviceregistry.di.Service;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface BukkitContextResolverService extends Service {
  Optional<Player> player(LogicContext ctx);
  Optional<World> world(LogicContext ctx);
  Optional<Location> location(LogicContext ctx);
  Optional<Block> block(LogicContext ctx);
  Optional<ItemStack> item(LogicContext ctx);
  Optional<Entity> entity(LogicContext ctx);
}