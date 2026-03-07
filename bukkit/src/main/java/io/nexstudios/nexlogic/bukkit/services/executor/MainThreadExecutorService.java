package io.nexstudios.nexlogic.bukkit.services.executor;

import io.nexstudios.serviceregistry.di.Service;

public interface MainThreadExecutorService extends Service {
  boolean isMainThread();
  void execute(Runnable task);
}