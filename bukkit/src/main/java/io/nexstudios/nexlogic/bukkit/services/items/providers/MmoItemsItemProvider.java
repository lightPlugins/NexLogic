package io.nexstudios.nexlogic.bukkit.services.items.providers;

import io.nexstudios.nexlogic.bukkit.services.hooks.mmoitems.MmoItemsService;
import io.nexstudios.nexlogic.bukkit.services.items.ItemProviderService;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class MmoItemsItemProvider implements ItemProviderService.Provider {

  private final MmoItemsService mmoItemsService;

  public MmoItemsItemProvider(MmoItemsService mmoItemsService) {
    this.mmoItemsService = mmoItemsService;
  }

  @Override
  public String providerName() {
    return "MMOItems";
  }

  @Override
  public String namespace() {
    return "mmoitems";
  }

  @Override
  public Optional<ItemStack> getItem(String namespacedId) {
    ParsedItem parsed = parse(namespacedId);
    if (parsed == null) {
      return Optional.empty();
    }

    if (parsed.level() != null && parsed.tier() != null) {
      return mmoItemsService.getMmoItemById(parsed.type(), parsed.id(), parsed.level(), parsed.tier());
    }

    return mmoItemsService.getMmoItemById(parsed.type(), parsed.id());
  }

  private static ParsedItem parse(String namespacedId) {
    String raw = namespacedId == null ? "" : namespacedId.trim();
    if (raw.isEmpty()) {
      return null;
    }

    String remainder = raw;
    int namespaceSeparator = raw.indexOf(':');
    if (namespaceSeparator >= 0 && namespaceSeparator + 1 < raw.length()) {
      remainder = raw.substring(namespaceSeparator + 1);
    }

    String[] segments = remainder.split(":");
    if (segments.length < 2) {
      return null;
    }

    String type = segments[0].trim();
    String id = segments[1].trim();
    if (type.isBlank() || id.isBlank()) {
      return null;
    }

    if (segments.length >= 4) {
      Integer level = parseInteger(segments[2]);
      String tier = segments[3].trim();
      if (level != null && !tier.isBlank()) {
        return new ParsedItem(type, id, level, tier);
      }
    }

    return new ParsedItem(type, id, null, null);
  }

  private static Integer parseInteger(String value) {
    if (value == null) {
      return null;
    }

    try {
      return Integer.valueOf(value.trim());
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  private record ParsedItem(String type, String id, Integer level, String tier) { }
}

