package io.nexstudios.nexlogic.bukkit.services.items.providers;

import io.nexstudios.nexlogic.bukkit.services.hooks.nexo.NexoService;
import io.nexstudios.nexlogic.bukkit.services.items.ItemProviderService;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class NexoItemProvider implements ItemProviderService.Provider {

  private final NexoService nexoService;

  public NexoItemProvider(NexoService nexoService) {
    this.nexoService = nexoService;
  }

  @Override
  public String providerName() {
    return "Nexo";
  }

  @Override
  public String namespace() {
    return "nexo";
  }

  @Override
  public Optional<ItemStack> getItem(String namespacedId) {
    String itemId = itemId(namespacedId);
    if (itemId.isBlank()) {
      return Optional.empty();
    }

    return nexoService.getNexoItemByID(itemId);
  }
}

