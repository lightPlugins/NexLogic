package io.nexstudios.nexlogic.bukkit.modules;

import io.nexstudios.nexlogic.bukkit.services.placeholder.DefaultPlaceholderReloadService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.PlaceholderReloadService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.loader.DefaultPlaceholderYamlLoaderService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.DefaultPlaceholderRuntimeService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.PlaceholderRuntimeService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.options.cache.DefaultPlaceholderCacheOptionsService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.options.resolve.DefaultPlaceholderResolveOptionsService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.options.resolve.PlaceholderResolveOptionsService;
import io.nexstudios.nexlogic.common.services.placeholder.DefaultPlaceholderService;
import io.nexstudios.nexlogic.common.services.placeholder.PlaceholderService;
import io.nexstudios.nexlogic.common.services.placeholder.cache.PlaceholderCacheOptionsService;
import io.nexstudios.nexlogic.common.services.placeholder.loader.PlaceholderYamlLoaderService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;

public final class PlaceholderServiceModule implements ServiceModule {

  @Override
  public void install(ServiceAccessor services) {
    // IO / Loader
    services.register(PlaceholderYamlLoaderService.class, DefaultPlaceholderYamlLoaderService.class);

    // Options (settings.yml)
    services.register(PlaceholderResolveOptionsService.class, DefaultPlaceholderResolveOptionsService.class);
    services.register(PlaceholderCacheOptionsService.class, DefaultPlaceholderCacheOptionsService.class);

    // Core placeholder registry + resolving
    services.register(PlaceholderService.class, DefaultPlaceholderService.class);

    // Reload & runtime helpers
    services.register(PlaceholderReloadService.class, DefaultPlaceholderReloadService.class);
    services.register(PlaceholderRuntimeService.class, DefaultPlaceholderRuntimeService.class);
  }
}