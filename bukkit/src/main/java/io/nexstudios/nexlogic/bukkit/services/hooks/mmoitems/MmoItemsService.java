package io.nexstudios.nexlogic.bukkit.services.hooks.mmoitems;

import io.nexstudios.serviceregistry.di.Service;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface MmoItemsService extends Service {

  Optional<ItemStack> getMmoItemById(String type, String id);
  Optional<ItemStack> getMmoItemById(String type, String id, int level, String tier);
}

