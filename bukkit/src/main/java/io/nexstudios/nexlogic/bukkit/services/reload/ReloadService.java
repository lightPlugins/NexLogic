package io.nexstudios.nexlogic.bukkit.services.reload;

import io.nexstudios.serviceregistry.di.Service;

public interface ReloadService extends Service {
  void reloadAsync();
}