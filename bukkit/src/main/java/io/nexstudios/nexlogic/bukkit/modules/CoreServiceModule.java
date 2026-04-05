package io.nexstudios.nexlogic.bukkit.modules;

import io.nexstudios.nexlogic.bukkit.services.effects.blocks.BlockKeyService;
import io.nexstudios.nexlogic.bukkit.services.effects.blocks.DefaultBlockKeyService;
import io.nexstudios.nexlogic.bukkit.services.effects.entities.DefaultEntityKeyService;
import io.nexstudios.nexlogic.bukkit.services.effects.entities.EntityKeyService;
import io.nexstudios.nexlogic.bukkit.services.effects.logging.BukkitLoggerService;
import io.nexstudios.nexlogic.bukkit.services.effects.platform.BukkitPlatformPluginService;
import io.nexstudios.nexlogic.bukkit.services.items.config.ConfigItemService;
import io.nexstudios.nexlogic.bukkit.services.items.config.DefaultConfigItemService;
import io.nexstudios.nexlogic.bukkit.services.items.DefaultItemProviderService;
import io.nexstudios.nexlogic.bukkit.services.items.ItemProviderService;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.nexlogic.common.services.platform.PlatformPluginService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;

public final class CoreServiceModule implements ServiceModule {

  @Override
  public void install(ServiceAccessor services) {
    services.register(LoggerService.class, BukkitLoggerService.class);
    services.register(PlatformPluginService.class, BukkitPlatformPluginService.class);

    services.register(BlockKeyService.class, DefaultBlockKeyService.class);
    services.register(EntityKeyService.class, DefaultEntityKeyService.class);
    services.register(ItemProviderService.class, DefaultItemProviderService.class);
    services.register(ConfigItemService.class, DefaultConfigItemService.class);
  }
}