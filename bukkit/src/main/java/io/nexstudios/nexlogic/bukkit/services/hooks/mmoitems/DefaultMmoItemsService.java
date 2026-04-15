package io.nexstudios.nexlogic.bukkit.services.hooks.mmoitems;


import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.Service;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.manager.TierManager;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Optional;

@Dependencies({
    LoggerService.class
})
public final class DefaultMmoItemsService implements MmoItemsService {

  private final LoggerService logger;

  public DefaultMmoItemsService(ServiceAccessor accessor) {
    this.logger = accessor.getService(LoggerService.class);
  }

  @Override
  public Optional<ItemStack> getMmoItemById(String type, String id) {

    MMOItem mmoItem = MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get(type), id);
    if(mmoItem == null) {
      logger.logger().warning("Could not find MMOItem with id '" + id + "' for type '" + type + "'");
      return Optional.empty();
    }
    return Optional.of(mmoItem.newBuilder().getItemStack());
  }

  @Override
  public Optional<ItemStack> getMmoItemById(String type, String id, int level, String tier) {

    TierManager tiers = MMOItems.plugin.getTiers();
    boolean exists = tiers.has(tier.toUpperCase(Locale.ROOT));

    if(!exists) {
      logger.logger().warning("Could not find tier '" + tier.toUpperCase(Locale.ROOT) + "' for item '" + type + ":" + id + "'");
      return Optional.empty();
    }

    ItemTier itemTier = tiers.get(tier.toUpperCase(Locale.ROOT));

    ItemStack stack = MMOItems.plugin.getItem(
        MMOItems.plugin.getTypes().get(type),
        id,
        level,
        itemTier);

    if(stack == null) {
      logger.logger().warning("Could not find itemstack for item '" + id + "' for type '" + type + "' with level '" + level + "' and tier '" + tier.toUpperCase(Locale.ROOT) + "'");
      return Optional.empty();
    }

    return Optional.of(stack);
  }
}

