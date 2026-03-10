package io.nexstudios.nexlogic.bukkit.modules;

import io.nexstudios.nexlogic.bukkit.services.effects.blocks.BlockKeyService;
import io.nexstudios.nexlogic.bukkit.services.effects.blocks.DefaultBlockKeyService;
import io.nexstudios.nexlogic.bukkit.services.effects.entities.DefaultEntityKeyService;
import io.nexstudios.nexlogic.bukkit.services.effects.entities.EntityKeyService;
import io.nexstudios.nexlogic.bukkit.services.effects.logging.BukkitLoggerService;
import io.nexstudios.nexlogic.bukkit.services.effects.platform.BukkitPlatformPluginService;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.nexlogic.common.services.platform.PlatformPluginService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;

public final class CoreModule implements ServiceModule {

  @Override
  public void install(ServiceAccessor services) {
    services.register(LoggerService.class, BukkitLoggerService.class);
    services.register(PlatformPluginService.class, BukkitPlatformPluginService.class);

    services.register(BlockKeyService.class, DefaultBlockKeyService.class);
    services.register(EntityKeyService.class, DefaultEntityKeyService.class);
  }
}