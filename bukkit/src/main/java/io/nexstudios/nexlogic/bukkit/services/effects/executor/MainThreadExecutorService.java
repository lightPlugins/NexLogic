package io.nexstudios.nexlogic.bukkit.services.effects.executor;

import io.nexstudios.serviceregistry.di.Service;

public interface MainThreadExecutorService extends Service {
  boolean isMainThread();
  void execute(Runnable task);
}