package io.nexstudios.nexlogic.bukkit.services.config;

import io.nexstudios.configservice.config.ConfigurationSection;
import io.nexstudios.itemservice.bukkit.service.item.ItemService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.Service;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

@Dependencies({
    ItemService.class
})
public class DefaultConfigItemService implements ConfigItemService {

  private final ItemService itemService;

  public DefaultConfigItemService(ServiceAccessor accessor) {
    this.itemService = accessor.getService(ItemService.class);
  }


  @Override
  public Optional<ItemStack> convertSectionToItem(ConfigurationSection section) {

    String rawItem = section.getString("item", "minecraft:stone");


    return Optional.empty();
  }

  @Override
  public Optional<List<ConfigEnchantment>> convertMapToEnchantment(ConfigurationSection section) {
    return Optional.empty();
  }

  @Override
  public Optional<List<ConfigAttribute>> convertMapToAttribute(ConfigurationSection section) {
    return Optional.empty();
  }
}
