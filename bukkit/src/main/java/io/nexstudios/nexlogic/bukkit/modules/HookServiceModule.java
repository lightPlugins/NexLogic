package io.nexstudios.nexlogic.bukkit.modules;

import io.nexstudios.nexlogic.bukkit.services.hooks.mythicmobs.DefaultMythicMobsService;
import io.nexstudios.nexlogic.bukkit.services.hooks.mythicmobs.MythicMobsService;
import io.nexstudios.nexlogic.bukkit.services.hooks.nexo.DefaultNexoService;
import io.nexstudios.nexlogic.bukkit.services.hooks.nexo.NexoService;
import io.nexstudios.serviceregistry.di.Service;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;
import org.bukkit.Bukkit;

public class HookServiceModule implements ServiceModule {


  @Override
  public void install(ServiceAccessor accessor) {

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
  }

  private static boolean isPluginEnabled(String pluginName) {
    return Bukkit.getServer().getPluginManager().isPluginEnabled(pluginName);
  }
}
