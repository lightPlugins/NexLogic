package io.nexstudios.nexlogic.bukkit.modules;

import io.nexstudios.nexlogic.bukkit.services.hooks.mythicmobs.DefaultMythicMobsService;
import io.nexstudios.nexlogic.bukkit.services.hooks.mythicmobs.MythicMobsService;
import io.nexstudios.nexlogic.bukkit.services.hooks.nexo.DefaultNexoService;
import io.nexstudios.nexlogic.bukkit.services.hooks.nexo.NexoService;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.Service;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;
import org.bukkit.Bukkit;

@Dependencies({
    LoggerService.class
})
public class HookServiceModule implements ServiceModule {

  private LoggerService logger;

  @Override
  public void install(ServiceAccessor accessor) {

    this.logger = accessor.getService(LoggerService.class);

    register(accessor, "Nexo", NexoService.class, DefaultNexoService.class);
    register(accessor, "MythicMobs", MythicMobsService.class, DefaultMythicMobsService.class);

  }

  private <T extends Service> void register(
      ServiceAccessor accessor,
      String pluginName,
      Class<T> serviceType,
      Class<? extends T> implClass
  ) {
    if (!isPluginEnabled(pluginName)) return;

    accessor.register(serviceType, implClass);
    logger.logger().info("Successfully hooked into " + pluginName);
  }

  private static boolean isPluginEnabled(String pluginName) {
    return Bukkit.getServer().getPluginManager().isPluginEnabled(pluginName);
  }
}
