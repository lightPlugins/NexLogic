package io.nexstudios.nexlogic.bukkit.services.items.config;

import io.nexstudios.configservice.config.ConfigurationSection;
import io.nexstudios.languageservice.service.language.LanguageService;
import io.nexstudios.serviceregistry.di.Service;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public interface ConfigItemService extends Service {

  Optional<ItemStack> convertSectionToItem(ConfigurationSection section);
  Optional<ItemStack> convertSectionToItem(ConfigurationSection section, LanguageService languageService, Player player);

  Optional<List<ConfigEnchantment>> convertMapToEnchantment(ConfigurationSection section);

  Optional<List<ConfigAttribute>> convertMapToAttribute(ConfigurationSection section);

  record ConfigAttribute(Attribute attribute, AttributeModifier.Operation operation, double amount, EquipmentSlotGroup group) { }

  record ConfigEnchantment(Enchantment enchantment, int level) { }
}
