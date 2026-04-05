package io.nexstudios.nexlogic.bukkit.modules;

import io.nexstudios.nexlogic.bukkit.services.hooks.mythicmobs.DefaultMythicMobsService;
import io.nexstudios.nexlogic.bukkit.services.hooks.mythicmobs.MythicMobsService;
import io.nexstudios.nexlogic.bukkit.services.hooks.mmoitems.DefaultMmoItemsService;
import io.nexstudios.nexlogic.bukkit.services.hooks.mmoitems.MmoItemsService;
import io.nexstudios.nexlogic.bukkit.services.hooks.nexo.DefaultNexoService;
import io.nexstudios.nexlogic.bukkit.services.hooks.nexo.NexoService;
import io.nexstudios.nexlogic.bukkit.services.hooks.towny.DefaultTownyService;
import io.nexstudios.nexlogic.bukkit.services.hooks.towny.TownyService;
import io.nexstudios.nexlogic.bukkit.services.items.ItemProviderService;
import io.nexstudios.nexlogic.bukkit.services.items.providers.MmoItemsItemProvider;
import io.nexstudios.nexlogic.bukkit.services.items.providers.NexoItemProvider;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.Service;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;
import org.bukkit.Bukkit;

@Dependencies({
    LoggerService.class,
    ItemProviderService.class
})
public class HookServiceModule implements ServiceModule {

  private LoggerService logger;
  private ItemProviderService itemProviderService;

  @Override
  public void install(ServiceAccessor accessor) {

    this.logger = accessor.getService(LoggerService.class);
    this.itemProviderService = accessor.getService(ItemProviderService.class);

    register(accessor, "Nexo", NexoService.class, DefaultNexoService.class);
    register(accessor, "MMOItems", MmoItemsService.class, DefaultMmoItemsService.class);
    register(accessor, "MythicMobs", MythicMobsService.class, DefaultMythicMobsService.class);
    register(accessor, "Towny", TownyService.class, DefaultTownyService.class);

  }

  private <T extends Service> void register(
      ServiceAccessor accessor,
      String pluginName,
      Class<T> serviceType,
      Class<? extends T> implClass
  ) {
    if (!isPluginEnabled(pluginName)) return;

    accessor.register(serviceType, implClass);
    if (serviceType == NexoService.class) {
      itemProviderService.registerProvider(new NexoItemProvider(accessor.getService(NexoService.class)));
    } else if (serviceType == MmoItemsService.class) {
      itemProviderService.registerProvider(new MmoItemsItemProvider(accessor.getService(MmoItemsService.class)));
    }
    logger.logger().info("Successfully hooked into " + pluginName);
  }

  private static boolean isPluginEnabled(String pluginName) {
    return Bukkit.getServer().getPluginManager().isPluginEnabled(pluginName);
  }
}
