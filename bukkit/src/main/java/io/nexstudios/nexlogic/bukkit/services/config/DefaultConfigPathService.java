package io.nexstudios.nexlogic.bukkit.services.config;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;

import java.nio.file.Files;
import java.nio.file.Path;

public final class DefaultConfigPathService implements ConfigPathService {

  private final Path dataFolder;

  public DefaultConfigPathService(PaperPluginService core) {
    this.dataFolder = core.plugin().getDataPath().toAbsolutePath();
    ensure();
  }

  @Override public Path dataFolder() { return dataFolder; }
  @Override public Path actionsDir() { return dataFolder.resolve("actions"); }
  @Override public Path argumentsDir() { return dataFolder.resolve("arguments"); }

  private void ensure() {
    try {
      Files.createDirectories(actionsDir());
      Files.createDirectories(argumentsDir());
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create NexLogic folders: " + e.getMessage(), e);
    }
  }
}