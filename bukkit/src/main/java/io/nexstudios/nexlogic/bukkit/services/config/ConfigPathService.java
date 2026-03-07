package io.nexstudios.nexlogic.bukkit.services.config;

import io.nexstudios.serviceregistry.di.Service;

import java.nio.file.Path;

public interface ConfigPathService extends Service {
  Path dataFolder();
  Path actionsDir();
  Path argumentsDir();
}