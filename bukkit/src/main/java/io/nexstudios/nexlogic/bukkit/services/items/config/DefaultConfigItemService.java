package io.nexstudios.nexlogic.bukkit.services.items.config;

import io.nexstudios.configservice.config.ConfigurationSection;
import io.nexstudios.itemservice.bukkit.service.item.ItemService;
import io.nexstudios.languageservice.service.language.LanguageService;
import io.nexstudios.nexlogic.bukkit.services.items.ItemProviderService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Dependencies({
    ItemService.class,
    ItemProviderService.class
})
public final class DefaultConfigItemService implements ConfigItemService {

  private final Registry<@NotNull Enchantment> enchantmentRegistry;
  private final Registry<@NotNull Attribute> attributeRegistry;
  private final ItemService itemService;
  private final ItemProviderService itemProviderService;

  public DefaultConfigItemService(ServiceAccessor accessor) {
    this.enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    this.attributeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ATTRIBUTE);
    this.itemService = accessor.getService(ItemService.class);
    this.itemProviderService = accessor.getService(ItemProviderService.class);
  }

  @Override
  public Optional<ItemStack> convertSectionToItem(ConfigurationSection section) {
    return convertSectionToItem(section, null, null);
  }

  @Override
  public Optional<ItemStack> convertSectionToItem(ConfigurationSection section, LanguageService languageService, Player player) {
    if (section == null) {
      return Optional.empty();
    }

    String rawItem = firstNonBlank(section.getString("item", ""), section.getString("material", ""));
    if (rawItem.isBlank()) {
      rawItem = "minecraft:stone";
    }

    ItemStack templateItem = itemProviderService.getItem(rawItem).orElse(null);
    Material baseMaterial = templateItem != null
        ? templateItem.getType()
        : resolveMaterial(rawItem).orElse(null);

    if (baseMaterial == null) {
      return Optional.empty();
    }

    var builder = templateItem != null
        ? itemService.builder(baseMaterial).of(templateItem.clone())
        : itemService.builder(baseMaterial).of(new ItemStack(baseMaterial));

    if (hasKey(section, "amount")) {
      int amount = section.getInt("amount", 1);
      if (amount > 0) {
        builder.amount(amount);
      }
    }

    if (hasKey(section, "display-name")) {
      String displayName = section.getString("display-name", null);
      if (displayName != null) {
        resolveConfiguredText(displayName, languageService, player).ifPresent(builder::name);
      }
    }

    if (hasKey(section, "lore")) {
      List<String> lore = readStringList(section, "lore");
      List<String> resolvedLore = resolveConfiguredLore(lore, languageService, player);
      if (!resolvedLore.isEmpty()) {
        builder.lore(l -> {
          for (String loreLine : resolvedLore) {
            l.line(loreLine);
          }
          l.build();
        });
      }
    }

    if (hasKey(section, "unbreakable")) {
      builder.unbreakable(section.getBoolean("unbreakable", false));
    }

    if (hasKey(section, "custom-model-data")) {
      builder.customModelData(section.getInt("custom-model-data", 0));
    }

    if (hasKey(section, "item-flags")) {
      List<ItemFlag> flags = new ArrayList<>();
      for (String rawFlag : readStringList(section, "item-flags")) {
        parseItemFlag(rawFlag).ifPresent(flags::add);
      }

      if (!flags.isEmpty()) {
        builder.flags(flags.toArray(ItemFlag[]::new));
      }
    }

    if (hasKey(section, "enchantments")) {
      convertMapToEnchantment(section).ifPresent(enchantments -> {
        for (ConfigEnchantment enchantment : enchantments) {
          if (enchantment != null && enchantment.enchantment() != null && enchantment.level() > 0) {
            builder.enchant(enchantment.enchantment(), enchantment.level());
          }
        }
      });
    }

    if (section.getBoolean("glowing", false)) {
      resolveEnchantment("minecraft:unbreaking")
          .ifPresent(enchantment -> builder.enchant(enchantment, 1).hideEnchants(true));
    }

    if (hasKey(section, "attributes")) {
      convertMapToAttribute(section).ifPresent(attributes -> {
        for (ConfigAttribute attribute : attributes) {
          if (attribute == null || attribute.attribute() == null || attribute.operation() == null || attribute.group() == null) {
            continue;
          }

          builder.attribute(
              attribute.attribute(),
              attribute.amount(),
              attribute.operation(),
              attribute.group()
          );
        }
      });
    }

    return Optional.of(builder.build());
  }

  @Override
  public Optional<List<ConfigEnchantment>> convertMapToEnchantment(ConfigurationSection section) {
    if (section == null) {
      return Optional.empty();
    }

    List<ConfigEnchantment> out = new ArrayList<>();

    List<ConfigurationSection> listEntries = section.getSectionList("enchantments");
    if (!listEntries.isEmpty()) {
      for (ConfigurationSection entry : listEntries) {
        parseEnchantmentEntry(entry).ifPresent(out::add);
      }
    } else {
      ConfigurationSection mapSection = section.getSection("enchantments");
      if (mapSection != null) {
        for (String key : mapSection.getKeys(false)) {
          Enchantment enchantment = resolveEnchantment(key).orElse(null);
          if (enchantment == null) {
            continue;
          }

          int level = mapSection.getInt(key, 1);
          if (level > 0) {
            out.add(new ConfigEnchantment(enchantment, level));
          }
        }
      }
    }

    return out.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(out));
  }

  @Override
  public Optional<List<ConfigAttribute>> convertMapToAttribute(ConfigurationSection section) {
    if (section == null) {
      return Optional.empty();
    }

    List<ConfigAttribute> out = new ArrayList<>();

    List<ConfigurationSection> listEntries = section.getSectionList("attributes");
    if (!listEntries.isEmpty()) {
      for (ConfigurationSection entry : listEntries) {
        parseAttributeEntry(entry).ifPresent(out::add);
      }
    } else {
      ConfigurationSection mapSection = section.getSection("attributes");
      if (mapSection != null) {
        for (String key : mapSection.getKeys(false)) {
          ConfigurationSection attributeSection = mapSection.getSection(key);
          if (attributeSection != null) {
            parseAttributeEntry(attributeSection, key).ifPresent(out::add);
          }
        }
      }
    }

    return out.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(out));
  }

  private Optional<ConfigEnchantment> parseEnchantmentEntry(ConfigurationSection entry) {
    if (entry == null) {
      return Optional.empty();
    }

    String id = firstNonBlank(entry.getString("id", ""), entry.getString("enchantment", ""));
    Enchantment enchantment = resolveEnchantment(id).orElse(null);
    if (enchantment == null) {
      return Optional.empty();
    }

    int level = entry.getInt("level", 1);
    if (level <= 0) {
      return Optional.empty();
    }

    return Optional.of(new ConfigEnchantment(enchantment, level));
  }

  private Optional<ConfigAttribute> parseAttributeEntry(ConfigurationSection entry) {
    return parseAttributeEntry(entry, null);
  }

  private Optional<ConfigAttribute> parseAttributeEntry(ConfigurationSection entry, String fallbackId) {
    if (entry == null) {
      return Optional.empty();
    }

    String id = firstNonBlank(
        firstNonBlank(entry.getString("id", ""), entry.getString("attribute", "")),
        fallbackId
    );

    Attribute attribute = resolveAttribute(id).orElse(null);
    if (attribute == null) {
      return Optional.empty();
    }

    AttributeModifier.Operation operation = parseOperation(entry.getString("operation", "ADD_NUMBER"));
    double amount = entry.getDouble("amount", 0.0);
    EquipmentSlotGroup group = parseGroup(entry.getString("group", "HAND"));

    return Optional.of(new ConfigAttribute(attribute, operation, amount, group));
  }

  private Optional<String> resolveConfiguredText(String rawText, LanguageService languageService, Player player) {
    if (rawText == null) {
      return Optional.empty();
    }

    String trimmed = rawText.trim();
    if (!isLanguageReference(trimmed) || languageService == null || player == null) {
      return trimmed.isEmpty() ? Optional.empty() : Optional.of(rawText);
    }

    String key = languageKey(trimmed);
    if (key.isBlank()) {
      return Optional.of(rawText);
    }

    List<String> translations = languageService.getTranslation(player, key);
    if (translations != null) {
      for (String translation : translations) {
        if (translation != null && !translation.isBlank()) {
          return Optional.of(translation);
        }
      }
    }

    return Optional.of(rawText);
  }

  private List<String> resolveConfiguredLore(List<String> rawLore, LanguageService languageService, Player player) {
    if (rawLore == null || rawLore.isEmpty()) {
      return List.of();
    }

    List<String> resolved = new ArrayList<>();

    for (String line : rawLore) {
      if (line == null) {
        continue;
      }

      String trimmed = line.trim();
      if (isLanguageReference(trimmed) && languageService != null && player != null) {
        String key = languageKey(trimmed);
        if (!key.isBlank()) {
          List<String> translations = languageService.getTranslation(player, key);
          if (translations != null && !translations.isEmpty()) {
            for (String translation : translations) {
              if (translation != null && !translation.isBlank()) {
                resolved.add(translation);
              }
            }
            continue;
          }
        }
      }

      if (!trimmed.isEmpty()) {
        resolved.add(line);
      }
    }

    return List.copyOf(resolved);
  }

  private static boolean isLanguageReference(String text) {
    return text != null && text.startsWith("language:");
  }

  private static String languageKey(String text) {
    if (text == null || !text.startsWith("language:")) {
      return "";
    }

    return text.substring("language:".length()).trim();
  }


  private Optional<Enchantment> resolveEnchantment(String rawId) {
    return resolveFromRegistry(enchantmentRegistry, rawId);
  }

  private Optional<Attribute> resolveAttribute(String rawId) {
    return resolveFromRegistry(attributeRegistry, rawId);
  }

  private static AttributeModifier.Operation parseOperation(String rawOperation) {
    String normalized = rawOperation == null
        ? "ADD_NUMBER"
        : rawOperation.trim().toUpperCase(Locale.ROOT);

    try {
      return AttributeModifier.Operation.valueOf(normalized);
    } catch (IllegalArgumentException ignored) {
      return AttributeModifier.Operation.ADD_NUMBER;
    }
  }

  private static EquipmentSlotGroup parseGroup(String rawGroup) {
    if (rawGroup == null || rawGroup.isBlank()) {
      return EquipmentSlotGroup.HAND;
    }

    String normalized = rawGroup.trim().toUpperCase(Locale.ROOT).replace('-', '_');

    return switch (normalized) {
      case "ANY" -> EquipmentSlotGroup.ANY;
      case "ARMOR" -> EquipmentSlotGroup.ARMOR;
      case "BODY" -> EquipmentSlotGroup.BODY;
      case "CHEST" -> EquipmentSlotGroup.CHEST;
      case "FEET" -> EquipmentSlotGroup.FEET;
      case "HEAD" -> EquipmentSlotGroup.HEAD;
      case "LEGS" -> EquipmentSlotGroup.LEGS;
      case "MAINHAND", "MAIN_HAND" -> EquipmentSlotGroup.MAINHAND;
      case "OFFHAND", "OFF_HAND" -> EquipmentSlotGroup.OFFHAND;
      case "SADDLE" -> EquipmentSlotGroup.SADDLE;
      default -> EquipmentSlotGroup.HAND;
    };
  }

  private static Optional<ItemFlag> parseItemFlag(String rawFlag) {
    if (rawFlag == null || rawFlag.isBlank()) {
      return Optional.empty();
    }

    String normalized = rawFlag.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    try {
      return Optional.of(ItemFlag.valueOf(normalized));
    } catch (IllegalArgumentException ignored) {
      return Optional.empty();
    }
  }

  private static List<String> readStringList(ConfigurationSection section, String path) {
    if (section == null || path == null || path.isBlank()) {
      return List.of();
    }

    Map<String, Object> values = section.getValues(false);
    if (values == null || !values.containsKey(path)) {
      return List.of();
    }

    Object raw = values.get(path);
    if (raw == null) {
      return List.of();
    }

    List<String> out = new ArrayList<>();

    if (raw instanceof List<?> list) {
      for (Object element : list) {
        if (element instanceof String string) {
          out.add(string);
          continue;
        }

        if (element instanceof ConfigurationSection nestedSection) {
          String value = nestedSection.getString("value", null);
          if (value != null && !value.isBlank()) {
            out.add(value);
          }
        }
      }

      return List.copyOf(out);
    }

    for (ConfigurationSection entry : section.getSectionList(path)) {
      if (entry == null) {
        continue;
      }

      String value = entry.getString("value", null);
      if (value != null && !value.isBlank()) {
        out.add(value);
      }
    }

    return List.copyOf(out);
  }

  private static boolean hasKey(ConfigurationSection section, String key) {
    return section != null
        && section.getValues(false) != null
        && section.getValues(false).containsKey(key);
  }

  private static String firstNonBlank(String first, String second) {
    String a = first == null ? "" : first.trim();
    if (!a.isBlank()) {
      return a;
    }

    return second == null ? "" : second.trim();
  }

  private static Optional<Material> resolveMaterial(String rawId) {
    String normalized = normalizeNamespacedId(rawId);
    if (normalized.isBlank()) {
      return Optional.empty();
    }

    Material material = Material.matchMaterial(normalized, true);
    if (material == null || material == Material.AIR) {
      return Optional.empty();
    }

    return Optional.of(material);
  }

  private static <T extends org.bukkit.Keyed> Optional<T> resolveFromRegistry(Registry<@NotNull T> registry, String rawId) {
    if (registry == null) {
      return Optional.empty();
    }

    NamespacedKey key = parseNamespacedKey(rawId).orElse(null);
    if (key == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(registry.get(key));
  }

  private static Optional<NamespacedKey> parseNamespacedKey(String rawId) {
    String normalized = normalizeNamespacedId(rawId);
    if (normalized.isBlank()) {
      return Optional.empty();
    }

    return Optional.ofNullable(NamespacedKey.fromString(normalized));
  }

  private static String normalizeNamespacedId(String rawId) {
    if (rawId == null) {
      return "";
    }

    String normalized = rawId.trim().toLowerCase(Locale.ROOT);
    if (normalized.isBlank()) {
      return "";
    }

    normalized = normalized.replace(' ', '_');

    if (!normalized.contains(":")) {
      return "minecraft:" + normalized;
    }

    return normalized;
  }
}