package io.nexstudios.nexlogic.bukkit.services.heads;

import io.nexstudios.serviceregistry.di.Service;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Provides access to player head items backed by cached data and persisted textures.
 * <p>
 * Implementations should return cached heads immediately when available and otherwise
 * resolve the head asynchronously without blocking the main server thread.
 */
public interface HeadService extends Service {

  /**
   * Returns a cached head for the given player UUID, if one is already available in memory.
   *
   * @param playerUuid the player's unique identifier
   * @return the cached head item, or an empty result if the head is not cached yet
   */
  Optional<ItemStack> getCachedHead(UUID playerUuid);

  /**
   * Loads a player head for the given UUID.
   * <p>
   * If the head is cached, the returned future should complete immediately.
   * Otherwise, the implementation should resolve the player profile asynchronously,
   * build the head item, cache it, and persist the texture data if needed.
   *
   * @param playerUuid the player's unique identifier
   * @return a future that completes with the player head item
   */
  CompletableFuture<ItemStack> loadHead(UUID playerUuid);

  /**
   * Warms up the head cache from persistent storage.
   * <p>
   * Implementations should load all saved head records, rebuild the corresponding
   * item stacks, and populate the in-memory cache before completing.
   *
   * @return a future that completes once warm-up has finished
   */
  CompletableFuture<Void> warmUp();

  /**
   * Removes a single player head from the in-memory cache.
   *
   * @param playerUuid the player's unique identifier
   */
  void invalidate(UUID playerUuid);

  /**
   * Clears all cached player heads from memory.
   */
  void clearCache();
}