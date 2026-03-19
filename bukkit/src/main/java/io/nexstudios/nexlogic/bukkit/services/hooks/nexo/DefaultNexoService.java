package io.nexstudios.nexlogic.bukkit.services.hooks.nexo;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

@NoArgsConstructor
public class DefaultNexoService implements NexoService {

  @Override
  public boolean isNexoItem(ItemStack itemStack) {
    return NexoItems.exists(itemStack);
  }

  @Override
  public Optional<ItemStack> getNexoItemByID(String nexoID) {
    ItemBuilder itemBuilder = NexoItems.itemFromId(nexoID);
    if(itemBuilder == null) {
      return Optional.empty();
    }
    ItemStack itemStack = itemBuilder.build();
    return Optional.of(itemStack);
  }

  @Override
  public boolean isNexoBlock(Block block) {
    return NexoBlocks.isCustomBlock(block);
  }

  @Override
  public void placeNexoBlock(String nexoBlockID, Location location) {
    NexoBlocks.place(nexoBlockID, location);
  }

  @Override
  public void removeNexoBlock(Location location) {
    NexoBlocks.remove(location);
  }
}
