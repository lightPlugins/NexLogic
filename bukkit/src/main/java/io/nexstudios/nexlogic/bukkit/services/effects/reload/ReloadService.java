package io.nexstudios.nexlogic.bukkit.services.effects.reload;

import io.nexstudios.serviceregistry.di.Service;

public interface ReloadService extends Service {
  void reloadAsync();
}