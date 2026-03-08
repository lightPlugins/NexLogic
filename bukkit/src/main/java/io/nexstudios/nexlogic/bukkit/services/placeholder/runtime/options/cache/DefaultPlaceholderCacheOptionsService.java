package io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.options.cache;

import io.nexstudios.framework.config.ConfigurationSection;
import io.nexstudios.framework.config.FileConfiguration;
import io.nexstudios.framework.config.service.singlereader.FileReaderService;
import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.services.placeholder.cache.PlaceholderCacheOptionsService;
import io.nexstudios.serviceregistry.di.Dependencies;

import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

@Dependencies({
    FileReaderService.class
})
public final class DefaultPlaceholderCacheOptionsService implements PlaceholderCacheOptionsService {

  private static final int FALLBACK_MAX_CACHE_ENTRIES = 100_000;

  private final Logger logger;
  private final FileConfiguration cfg;

  private volatile int maxCacheEntries = FALLBACK_MAX_CACHE_ENTRIES;

  public DefaultPlaceholderCacheOptionsService(PaperPluginService core) {
    Objects.requireNonNull(core, "core");

    FileReaderService reader = core.plugin().services().getService(FileReaderService.class);
    this.logger = core.plugin().getLogger();

    this.cfg = reader.load(
        Path.of("settings.yml"),
        "settings.yml",
        true
    );

    reload();
  }

  @Override
  public int maxCacheEntries() {
    return maxCacheEntries;
  }

  @Override
  public void reload() {
    try {
      cfg.reload();

      ConfigurationSection placeholder = cfg.getSection("placeholder");
      ConfigurationSection cache = placeholder == null ? null : placeholder.getSection("cache");

      int v = cache == null
          ? FALLBACK_MAX_CACHE_ENTRIES
          : cache.getInt("max-cache-entries", FALLBACK_MAX_CACHE_ENTRIES);

      if (v < 1) {
        logger.warning("Invalid placeholder.cache.max-cache-entries (" + v + "), using fallback " + FALLBACK_MAX_CACHE_ENTRIES);
        v = FALLBACK_MAX_CACHE_ENTRIES;
      }

      maxCacheEntries = v;
    } catch (Throwable t) {
      maxCacheEntries = FALLBACK_MAX_CACHE_ENTRIES;
      logger.severe("Failed to reload placeholder cache options, using defaults: " + t.getMessage());
      t.printStackTrace();
    }
  }
}