package io.nexstudios.nexlogic.bukkit.services.effects.reload;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.async.AsyncExecutorService;
import io.nexstudios.nexlogic.bukkit.services.effects.loader.YamlLoaderService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.PlaceholderReloadService;
import io.nexstudios.nexlogic.common.services.placeholder.cache.PlaceholderCacheOptionsService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.options.resolve.PlaceholderResolveOptionsService;
import io.nexstudios.nexlogic.common.services.engine.LogicEngineService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Dependencies({
    AsyncExecutorService.class,
    YamlLoaderService.class,
    LogicEngineService.class,
    PlaceholderReloadService.class,
    PlaceholderResolveOptionsService.class,
    PlaceholderCacheOptionsService.class
})
public final class DefaultReloadService implements ReloadService {

  private final ServiceAccessor services;
  private final AsyncExecutorService async;
  private final YamlLoaderService loader;
  private final LogicEngineService engine;
  private final PlaceholderReloadService placeholders;
  private final PlaceholderResolveOptionsService placeholderOptions;
  private final PlaceholderCacheOptionsService placeholderCacheOptions;
  private final Logger logger;

  private final Set<String> lastOwners = ConcurrentHashMap.newKeySet();

  public DefaultReloadService(PaperPluginService core) {
    this.services = core.plugin().services();
    this.async = services.getService(AsyncExecutorService.class);
    this.loader = services.getService(YamlLoaderService.class);
    this.engine = services.getService(LogicEngineService.class);
    this.placeholders = services.getService(PlaceholderReloadService.class);
    this.placeholderOptions = services.getService(PlaceholderResolveOptionsService.class);
    this.placeholderCacheOptions = services.getService(PlaceholderCacheOptionsService.class);
    this.logger = core.plugin().getLogger();
  }

  @Override
  public void reloadAsync() {
    CompletableFuture<Void> f = async.runAsync(() -> {
      logger.info("Reload started...");

      try {
        // Reload resolve limits first
        placeholderOptions.reload();
        placeholderCacheOptions.reload();

        // Reload placeholders before effects (effects may reference them at runtime)
        placeholders.reloadAll();

        var args = loader.loadArguments();
        var packs = loader.loadEffectStylePacks(args);

        logger.info("Reload: loaded effect-style packs owners=" + packs.size());

        Set<String> now = new HashSet<>(packs.keySet());
        for (String old : new HashSet<>(lastOwners)) {
          if (!now.contains(old)) {
            engine.unregisterOwner(old);
            lastOwners.remove(old);
          }
        }

        for (var e : packs.entrySet()) {
          engine.registerEffectStyle(e.getKey(), e.getValue());
          lastOwners.add(e.getKey());
        }

        logger.info("Reload completed. effectStyleOwners=" + packs.size());
      } catch (Throwable t) {
        logger.severe("Reload FAILED: " + t.getMessage());
        t.printStackTrace();
      } finally {
        logger.info("Reload finished (async task ended).");
      }
    });

    f.whenComplete((ok, ex) -> {
      if (ex != null) {
        logger.severe("Reload async future completed exceptionally: " + ex.getMessage());
        ex.printStackTrace();
      }
    });
  }
}