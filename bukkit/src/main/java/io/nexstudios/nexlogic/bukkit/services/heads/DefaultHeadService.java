package io.nexstudios.nexlogic.bukkit.services.heads;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.nexstudios.databaseservice.bukkit.service.api.DatabaseService;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.MainThreadExecutorService;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.async.AsyncExecutorService;
import io.nexstudios.nexlogic.bukkit.services.entity.heads.HeadEntity;
import io.nexstudios.nexlogic.bukkit.services.heads.repository.DefaultHeadRepository;
import io.nexstudios.nexlogic.bukkit.services.heads.repository.HeadRepository;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Dependencies({
    AsyncExecutorService.class,
    MainThreadExecutorService.class,
    LoggerService.class,
    DatabaseService.class
})
public final class DefaultHeadService implements HeadService {

  private static final String TEXTURE_PROPERTY = "textures";

  private final MainThreadExecutorService mainThread;
  private final AsyncExecutorService async;
  private final Logger logger;
  private final HeadRepository repository;
  private final ConcurrentHashMap<UUID, CachedHead> cache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<UUID, CompletableFuture<ItemStack>> inFlightLoads = new ConcurrentHashMap<>();

  public DefaultHeadService(ServiceAccessor services) {
    this.mainThread = services.getService(MainThreadExecutorService.class);
    this.async = services.getService(AsyncExecutorService.class);
    this.logger = services.getService(LoggerService.class).logger();
    this.repository = new DefaultHeadRepository(services);
  }

  @Override
  public Optional<ItemStack> getCachedHead(UUID playerUuid) {
    Objects.requireNonNull(playerUuid, "playerUuid");
    CachedHead cached = cache.get(playerUuid);
    return cached == null ? Optional.empty() : Optional.of(cached.copy());
  }

  @Override
  public CompletableFuture<ItemStack> loadHead(UUID playerUuid) {
    Objects.requireNonNull(playerUuid, "playerUuid");

    CachedHead cached = cache.get(playerUuid);
    if (cached != null) {
      return CompletableFuture.completedFuture(cached.copy());
    }

    return inFlightLoads.computeIfAbsent(playerUuid, this::loadHeadInternal);
  }

  @Override
  public CompletableFuture<Void> warmUp() {
    return async.runAsync(this::warmUpSync);
  }

  @Override
  public void warmUpSync() {
    try {
      List<HeadEntity> entities = repository.findAll().join();
      int loadedCount = cacheRestoredHeadsSync(entities);
      logger.info("HeadService warm-up finished. Loaded " + loadedCount + " player heads.");
    } catch (Throwable ex) {
      logger.warning("Failed to warm up cached player heads: " + ex.getMessage());
    }
  }

  @Override
  public void invalidate(UUID playerUuid) {
    if (playerUuid == null) {
      return;
    }
    cache.remove(playerUuid);
  }

  @Override
  public void clearCache() {
    cache.clear();
  }

  private int cacheRestoredHeadsSync(List<HeadEntity> entities) {
    int loadedCount = 0;
    for (HeadEntity entity : entities) {
      if (entity == null || entity.getPlayerUuid() == null || isBlank(entity.getTextureBase64())) {
        continue;
      }

      ItemStack item = buildHeadItem(entity.getPlayerUuid(), entity.getPlayerName(), entity.getTextureBase64(), null);
      cache.put(entity.getPlayerUuid(), new CachedHead(item.clone(), normalizeName(entity.getPlayerName()), entity.getTextureBase64()));
      loadedCount++;
    }

    return loadedCount;
  }

  private CompletableFuture<ItemStack> loadHeadInternal(UUID playerUuid) {
    return repository.findByPlayerUuid(playerUuid)
        .thenCompose(optionalEntity -> {
          HeadEntity entity = optionalEntity.orElse(null);
          if (entity != null && !isBlank(entity.getTextureBase64())) {
                    return buildCachedHeadFromTexture(entity.getPlayerUuid(), entity.getPlayerName(), entity.getTextureBase64());
          }

          String fallbackName = entity == null ? null : entity.getPlayerName();
          return resolveProfile(playerUuid, fallbackName)
              .thenCompose(profile -> persistAndBuild(playerUuid, profile, entity));
        })
        .whenComplete((item, throwable) -> inFlightLoads.remove(playerUuid));
  }

  private CompletableFuture<ItemStack> persistAndBuild(UUID playerUuid, PlayerProfile profile, HeadEntity existing) {
    String texture = extractTexture(profile);
    String playerName = profile.getName();

    if (isBlank(texture)) {
      return buildAndCache(playerUuid, playerName, null, profile);
    }

    String normalizedName = normalizeName(playerName);
    String normalizedTexture = texture.trim();
    boolean shouldPersist = existing == null
        || !Objects.equals(normalizedTexture, existing.getTextureBase64())
        || !Objects.equals(normalizedName, normalizeName(existing.getPlayerName()));

    CompletableFuture<Void> persistFuture = shouldPersist
        ? repository.upsert(playerUuid, normalizedName, normalizedTexture)
            .thenAccept(saved -> {
              if (saved == null) {
                logger.warning("Resolved head texture for " + playerUuid + " was not persisted.");
              }
            })
            .exceptionally(ex -> {
              logger.warning("Failed to persist resolved head texture for " + playerUuid + ": " + ex.getMessage());
              return null;
            })
        : CompletableFuture.completedFuture(null);

    return persistFuture.thenCompose(ignored -> buildAndCache(playerUuid, normalizedName, normalizedTexture, profile));
  }

  private CompletableFuture<ItemStack> buildAndCache(UUID playerUuid, String playerName, String textureBase64, PlayerProfile profile) {
    String normalizedTexture = isBlank(textureBase64) ? null : textureBase64.trim();

    return runOnMainThread(() -> buildHeadItem(playerUuid, playerName, normalizedTexture, profile))
        .thenApply(item -> {
          cache.put(playerUuid, new CachedHead(item.clone(), normalizeName(playerName), normalizedTexture));
          return item;
        });
  }

  private CompletableFuture<ItemStack> buildCachedHeadFromTexture(UUID playerUuid, String playerName, String textureBase64) {
    return runOnMainThread(() -> {
      ItemStack item = buildHeadItem(playerUuid, playerName, textureBase64, null);
      cache.put(playerUuid, new CachedHead(item.clone(), normalizeName(playerName), textureBase64));
      return item;
    });
  }

  private CompletableFuture<PlayerProfile> resolveProfile(UUID playerUuid, String fallbackName) {
    return findOnlineProfile(playerUuid).thenCompose(optional -> {
      if (optional.isPresent()) {
        return CompletableFuture.completedFuture(optional.get());
      }

      PlayerProfile profile = fallbackName == null
          ? Bukkit.createProfile(playerUuid)
          : Bukkit.createProfileExact(playerUuid, fallbackName);

      return profile.update().exceptionally(ex -> {
        logger.warning("Failed to update player profile for " + playerUuid + ": " + ex.getMessage());
        return profile;
      });
    });
  }

  private CompletableFuture<Optional<PlayerProfile>> findOnlineProfile(UUID playerUuid) {
    return runOnMainThread(() -> {
      Player player = Bukkit.getPlayer(playerUuid);
      return player == null ? Optional.empty() : Optional.of(player.getPlayerProfile());
    });
  }

  private <T> CompletableFuture<T> runOnMainThread(SupplierWithException<T> supplier) {
    CompletableFuture<T> result = new CompletableFuture<>();
    mainThread.execute(() -> {
      try {
        result.complete(supplier.get());
      } catch (Throwable t) {
        result.completeExceptionally(t);
      }
    });
    return result;
  }

  private ItemStack buildHeadItem(UUID playerUuid, String playerName, String textureBase64, PlayerProfile profile) {
    PlayerProfile effectiveProfile = profile;
    if (effectiveProfile == null) {
      effectiveProfile = playerName == null
          ? Bukkit.createProfile(playerUuid)
          : Bukkit.createProfileExact(playerUuid, playerName);
      if (!isBlank(textureBase64)) {
        effectiveProfile.setProperty(new ProfileProperty(TEXTURE_PROPERTY, textureBase64));
      }
    } else if (!isBlank(textureBase64) && !hasTextureProperty(effectiveProfile)) {
      effectiveProfile.setProperty(new ProfileProperty(TEXTURE_PROPERTY, textureBase64));
    }

    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
    SkullMeta meta = (SkullMeta) item.getItemMeta();
    if (meta == null) {
      return item;
    }

    meta.setPlayerProfile(effectiveProfile);
    item.setItemMeta(meta);
    return item;
  }

  private static boolean hasTextureProperty(PlayerProfile profile) {
    for (ProfileProperty property : profile.getProperties()) {
      if (TEXTURE_PROPERTY.equalsIgnoreCase(property.getName())) {
        return true;
      }
    }
    return false;
  }

  private static String extractTexture(PlayerProfile profile) {
    for (ProfileProperty property : profile.getProperties()) {
      if (TEXTURE_PROPERTY.equalsIgnoreCase(property.getName())) {
        return property.getValue();
      }
    }
    return null;
  }

  private static String normalizeName(String playerName) {
    if (playerName == null) {
      return null;
    }
    String normalized = playerName.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private record CachedHead(ItemStack itemStack, String playerName, String textureBase64) {
    private ItemStack copy() {
      return itemStack.clone();
    }
  }

  @FunctionalInterface
  private interface SupplierWithException<T> {
    T get() throws Exception;
  }
}



