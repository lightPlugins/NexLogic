package io.nexstudios.nexlogic.bukkit.services.effects.reload;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.languageservice.service.language.LanguageService;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.async.AsyncExecutorService;
import io.nexstudios.nexlogic.bukkit.services.effects.loader.YamlLoaderService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.PlaceholderReloadService;
import io.nexstudios.nexlogic.common.services.placeholder.cache.PlaceholderCacheOptionsService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.options.resolve.PlaceholderResolveOptionsService;
import io.nexstudios.nexlogic.common.services.engine.LogicEngineService;
import io.nexstudios.nexlogic.common.services.triggers.bus.TriggerBusService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Dependencies({
    AsyncExecutorService.class,
    YamlLoaderService.class,
    LogicEngineService.class,
    PlaceholderReloadService.class,
    PlaceholderResolveOptionsService.class,
    PlaceholderCacheOptionsService.class,
    LanguageService.class,
    TriggerBusService.class
})
public final class DefaultReloadService implements ReloadService {

  private final ServiceAccessor services;
  private final AsyncExecutorService async;
  private final YamlLoaderService loader;
  private final LogicEngineService engine;
  private final TriggerBusService triggerBus;
  private final PlaceholderReloadService placeholders;
  private final PlaceholderResolveOptionsService placeholderOptions;
  private final PlaceholderCacheOptionsService placeholderCacheOptions;
  private final LanguageService languageService;
  private final Logger logger;

  private final Object lastOwnersMutex = new Object();
  private final Set<String> lastOwners = new HashSet<>();

  public DefaultReloadService(PaperPluginService core) {
    this.services = core.plugin().services();
    this.async = services.getService(AsyncExecutorService.class);
    this.loader = services.getService(YamlLoaderService.class);
    this.engine = services.getService(LogicEngineService.class);
    this.triggerBus = services.getService(TriggerBusService.class);
    this.placeholders = services.getService(PlaceholderReloadService.class);
    this.placeholderOptions = services.getService(PlaceholderResolveOptionsService.class);
    this.placeholderCacheOptions = services.getService(PlaceholderCacheOptionsService.class);
    this.languageService = services.getService(LanguageService.class);
    this.logger = core.plugin().getLogger();
  }

  @Override
  public void reloadAsync() {
    CompletableFuture<Void> f = async.runAsync(() -> {
      logger.info("Reload started...");

      try {
        languageService.reload();
        placeholderOptions.reload();
        placeholderCacheOptions.reload();

        placeholders.reloadAll();

        var args = loader.loadArguments();
        var packs = loader.loadEffectStylePacks(args);

        logger.info("Reload: loaded effect-style packs owners=" + packs.size());

        synchronized (lastOwnersMutex) {
          Set<String> now = new HashSet<>(packs.keySet());
          Set<String> oldCopy = new HashSet<>(lastOwners);
          
          for (String old : oldCopy) {
            if (!now.contains(old)) {
              engine.unregisterOwner(old);
              lastOwners.remove(old);
            }
          }

          for (var e : packs.entrySet()) {
            engine.registerEffectStyle(e.getKey(), e.getValue());
            lastOwners.add(e.getKey());
          }
        }

        logger.info("Reload completed. effectStyleOwners=" + packs.size());
      } catch (Throwable t) {
        logger.severe("Reload FAILED: " + t.getClass().getSimpleName() + " " + t.getMessage());
      } finally {
        logger.info("Reload finished (async task ended).");
      }
    });

    f.whenComplete((ok, ex) -> {
      if (ex != null) {
        logger.severe("Reload async future failed: " + ex.getClass().getSimpleName() + " " + ex.getMessage());
      }
    });
  }

  @Override
  public void reloadSync() {
    reloadAsync();
  }
}