package io.nexstudios.nexlogic.bukkit.services.effects.executor;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class DefaultMainThreadExecutorService implements MainThreadExecutorService {

  private final Plugin plugin;

  public DefaultMainThreadExecutorService(PaperPluginService core) {
    this.plugin = core.plugin();
  }

  @Override
  public boolean isMainThread() {
    return Bukkit.isPrimaryThread();
  }

  @Override
  public void execute(Runnable task) {
    if (Bukkit.isPrimaryThread()) task.run();
    else Bukkit.getScheduler().runTask(plugin, task);
  }
}