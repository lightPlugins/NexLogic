package io.nexstudios.nexlogic.bukkit.services.heads.repository;

import io.nexstudios.nexlogic.bukkit.services.entity.heads.HeadEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Provides persistence access for player head data.
 * <p>
 * Implementations are responsible for loading persisted head records,
 * looking up a single record by player UUID, and upserting texture data
 * when a head is resolved or refreshed.
 */
public interface HeadRepository {

  /**
   * Loads all persisted head records from storage.
   *
   * @return a future that completes with all stored head entities
   */
  CompletableFuture<List<HeadEntity>> findAll();

  /**
   * Loads a single persisted head record by player UUID.
   *
   * @param playerUuid the player's unique identifier
   * @return a future that completes with the matching head entity, if present
   */
  CompletableFuture<Optional<HeadEntity>> findByPlayerUuid(UUID playerUuid);

  /**
   * Inserts a new head record or updates an existing one.
   * <p>
   * The Base64 texture value is treated as the canonical persisted texture
   * representation.
   *
   * @param playerUuid the player's unique identifier
   * @param playerName the player's current name, if available
   * @param textureBase64 the Base64-encoded texture value
   * @return a future that completes with the persisted head entity
   */
  CompletableFuture<HeadEntity> upsert(UUID playerUuid, String playerName, String textureBase64);
}