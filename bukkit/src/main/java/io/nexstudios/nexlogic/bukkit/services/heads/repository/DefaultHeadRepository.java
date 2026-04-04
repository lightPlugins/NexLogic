package io.nexstudios.nexlogic.bukkit.services.heads.repository;

import io.nexstudios.databaseservice.bukkit.service.api.DatabaseService;
import io.nexstudios.nexlogic.bukkit.services.entity.heads.HeadEntity;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.async.AsyncExecutorService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public final class DefaultHeadRepository implements HeadRepository {

  private final AsyncExecutorService async;
  private final Object databaseHandle;

  public DefaultHeadRepository(ServiceAccessor services) {
    this.async = services.getService(AsyncExecutorService.class);
    this.databaseHandle = services.getService(DatabaseService.class);
    validateEntityManagerAccess();
  }

  @Override
  public CompletableFuture<List<HeadEntity>> findAll() {
    return submit(() -> inEntityManager(em -> {
      List<HeadEntity> entities = em.createQuery("select h from HeadEntity h", HeadEntity.class).getResultList();
      List<HeadEntity> detached = new ArrayList<>(entities.size());
      for (HeadEntity entity : entities) {
        detached.add(copy(entity));
      }
      return List.copyOf(detached);
    }));
  }

  @Override
  public CompletableFuture<Optional<HeadEntity>> findByPlayerUuid(UUID playerUuid) {
    Objects.requireNonNull(playerUuid, "playerUuid");
    return submit(() -> inEntityManager(em -> Optional.ofNullable(em.find(HeadEntity.class, playerUuid)).map(DefaultHeadRepository::copy)));
  }

  @Override
  public CompletableFuture<HeadEntity> upsert(UUID playerUuid, String playerName, String textureBase64) {
    Objects.requireNonNull(playerUuid, "playerUuid");
    if (textureBase64 == null || textureBase64.isBlank()) {
      throw new IllegalArgumentException("textureBase64 is required");
    }

    String normalizedName = normalizeName(playerName);
    String normalizedTexture = textureBase64.trim();

    return submit(() -> inEntityManager(em -> {
      HeadEntity entity = em.find(HeadEntity.class, playerUuid);
      if (entity == null) {
        entity = HeadEntity.builder()
            .playerUuid(playerUuid)
            .playerName(normalizedName)
            .textureBase64(normalizedTexture)
            .build();
        em.persist(entity);
        return copy(entity);
      }

      boolean changed = false;
      if (!Objects.equals(normalizedName, normalizeName(entity.getPlayerName()))) {
        entity.setPlayerName(normalizedName);
        changed = true;
      }
      if (!Objects.equals(normalizedTexture, entity.getTextureBase64())) {
        entity.setTextureBase64(normalizedTexture);
        changed = true;
      }

      if (changed) {
        entity = em.merge(entity);
      }

      return copy(entity);
    }));
  }

  private <T> CompletableFuture<T> submit(Supplier<T> supplier) {
    CompletableFuture<T> result = new CompletableFuture<>();
    async.runAsync(() -> {
      try {
        result.complete(supplier.get());
      } catch (Throwable t) {
        result.completeExceptionally(t);
      }
    });
    return result;
  }

  private <T> T inEntityManager(@NonNull Function<EntityManager, T> action) {
    EntityManager entityManager = createEntityManager();
    boolean started = false;
    EntityTransaction transaction = null;
    try (entityManager) {
      transaction = entityManager.getTransaction();
      if (!transaction.isActive()) {
        transaction.begin();
        started = true;
      }
      T value = action.apply(entityManager);
      if (started && transaction.isActive()) {
        transaction.commit();
      }
      return value;
    } catch (RuntimeException | Error ex) {
      if (started && transaction.isActive()) {
        transaction.rollback();
      }
      throw ex;
    }
  }

  private EntityManager createEntityManager() {
    if (databaseHandle instanceof EntityManagerFactory factory) {
      return factory.createEntityManager();
    }

    Object candidate = invokeNoArg(databaseHandle, "createEntityManager");
    if (candidate instanceof EntityManager entityManager) {
      return entityManager;
    }

    Object factoryCandidate = invokeNoArg(databaseHandle, "entityManagerFactory", "getEntityManagerFactory", "sessionFactory", "getSessionFactory");
    if (factoryCandidate instanceof EntityManagerFactory factory) {
      return factory.createEntityManager();
    }

    throw new IllegalStateException("Unable to locate an EntityManagerFactory or createEntityManager() method on database service " + databaseHandle.getClass().getName());
  }

  private void validateEntityManagerAccess() {
    EntityManager entityManager = createEntityManager();
    entityManager.close();
  }

  private static Object invokeNoArg(Object target, String... methodNames) {
    for (String methodName : methodNames) {
      try {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
      } catch (ReflectiveOperationException ignored) {
      }
    }
    return null;
  }

  private static HeadEntity copy(HeadEntity entity) {
    if (entity == null) {
      return null;
    }
    return HeadEntity.builder()
        .playerUuid(entity.getPlayerUuid())
        .playerName(entity.getPlayerName())
        .textureBase64(entity.getTextureBase64())
        .rowVersion(entity.getRowVersion())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  private static String normalizeName(String playerName) {
    if (playerName == null) {
      return null;
    }
    String normalized = playerName.trim();
    return normalized.isEmpty() ? null : normalized;
  }
}


