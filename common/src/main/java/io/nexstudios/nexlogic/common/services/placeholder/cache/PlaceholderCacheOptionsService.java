package io.nexstudios.nexlogic.common.services.placeholder.cache;

import io.nexstudios.serviceregistry.di.Service;

public interface PlaceholderCacheOptionsService extends Service {

  /**
   * Maximum number of cache entries across all placeholders/scopes.
   * Must be >= 1.
   */
  int maxCacheEntries();

  /**
   * Reloads options from config.
   */
  void reload();
}