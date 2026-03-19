package io.nexstudios.nexlogic.bukkit.services.hooks.nexo;

import io.nexstudios.serviceregistry.di.Service;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * The {@code NexoService} interface provides methods to interact with Nexo items and blocks.
 * It serves as an abstraction layer to handle operations related to Nexo items and custom blocks.
 */
public interface NexoService extends Service {

  /**
   * Checks if the given {@code ItemStack} corresponds to a Nexo item.
   *
   * @param itemStack the {@code ItemStack} to check
   * @return {@code true} if the {@code ItemStack} is a Nexo item, {@code false} otherwise
   */
  boolean isNexoItem(ItemStack itemStack);

  /**
   * Retrieves an Optional containing the {@code ItemStack} representation of a Nexo item
   * corresponding to the provided Nexo ID. If the Nexo ID does not exist or cannot be resolved,
   * an empty Optional is returned.
   *
   * @param nexoID the identifier of the Nexo item to retrieve
   * @return an Optional containing the {@code ItemStack} if found, or an empty Optional if not
   */
  Optional<ItemStack> getNexoItemByID(String nexoID);

  /**
   * Checks if the specified {@code Block} is a Nexo block.
   *
   * @param block the {@code Block} to check
   * @return {@code true} if the {@code Block} is a Nexo block, {@code false} otherwise
   */
  boolean isNexoBlock(Block block);

  /**
   * Places a Nexo block at the specified location using the provided Nexo block ID.
   *
   * @param nexoBlockID the identifier of the Nexo block to place
   * @param location the location where the Nexo block should be placed
   */
  void placeNexoBlock(String nexoBlockID, Location location);

  /**
   * Removes a Nexo block from the specified location.
   *
   * @param location the location where the Nexo block should be removed
   */
  void removeNexoBlock(Location location);
}
