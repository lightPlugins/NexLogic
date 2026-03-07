package io.nexstudios.nexlogic.bukkit.services.listeners;

import io.nexstudios.serviceregistry.di.Service;

public interface TriggerListenerService extends Service {
  void registerAll();
}