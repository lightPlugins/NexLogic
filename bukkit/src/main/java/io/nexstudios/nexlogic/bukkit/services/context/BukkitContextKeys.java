package io.nexstudios.nexlogic.bukkit.services.context;

import io.nexstudios.nexlogic.common.model.ContextKey;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class BukkitContextKeys {

  private BukkitContextKeys() {}

  public static final ContextKey<Player> PLAYER = new ContextKey<>("bukkit:player", Player.class);
  public static final ContextKey<World> WORLD = new ContextKey<>("bukkit:world", World.class);
  public static final ContextKey<Location> LOCATION = new ContextKey<>("bukkit:location", Location.class);
  public static final ContextKey<Block> BLOCK = new ContextKey<>("bukkit:block", Block.class);
  public static final ContextKey<ItemStack> ITEM_STACK = new ContextKey<>("bukkit:itemstack", ItemStack.class);
}