package io.nexstudios.nexlogic.bukkit.services.items.providers;

import io.nexstudios.nexlogic.bukkit.services.items.ItemProviderService;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Optional;

public final class VanillaItemProvider implements ItemProviderService.Provider {

  @Override
  public String providerName() {
    return "Vanilla";
  }

  @Override
  public String namespace() {
    return "minecraft";
  }

  @Override
  public Optional<ItemStack> getItem(String namespacedId) {
    String itemId = itemId(namespacedId);
    if (itemId.isBlank()) {
      return Optional.empty();
    }

    Material material = Material.matchMaterial(itemId.toUpperCase(Locale.ROOT));
    if (material == null || material == Material.AIR) {
      return Optional.empty();
    }

    return Optional.of(new ItemStack(material));
  }
}

