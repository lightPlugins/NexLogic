package io.nexstudios.nexlogic.bukkit.services.items;

import io.nexstudios.serviceregistry.di.Service;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface ItemProviderService extends Service {

  interface Provider {

    String providerName();

    String namespace();

    Optional<ItemStack> getItem(String namespacedId);

    default boolean supports(String namespacedId) {
      return namespacedId != null && namespacedId.startsWith(namespace() + ":");
    }

    default String itemId(String namespacedId) {
      int separatorIndex = namespacedId == null ? -1 : namespacedId.indexOf(':');
      return separatorIndex < 0 || separatorIndex + 1 >= namespacedId.length()
          ? ""
          : namespacedId.substring(separatorIndex + 1);
    }
  }

  void registerProvider(Provider provider);

  Optional<ItemStack> getItem(String namespace);

}
