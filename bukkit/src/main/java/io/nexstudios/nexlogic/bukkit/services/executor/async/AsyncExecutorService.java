package io.nexstudios.nexlogic.bukkit.services.executor.async;

import io.nexstudios.serviceregistry.di.Service;

import java.util.concurrent.CompletableFuture;

public interface AsyncExecutorService extends Service {
  CompletableFuture<Void> runAsync(Runnable task);
  void shutdown();
}