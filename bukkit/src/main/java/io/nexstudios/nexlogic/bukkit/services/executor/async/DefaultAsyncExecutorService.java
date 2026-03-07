package io.nexstudios.nexlogic.bukkit.services.executor.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DefaultAsyncExecutorService implements AsyncExecutorService {

  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

  @Override
  public CompletableFuture<Void> runAsync(Runnable task) {
    return CompletableFuture.runAsync(task, executor);
  }

  @Override
  public void shutdown() {
    executor.shutdown();
  }
}