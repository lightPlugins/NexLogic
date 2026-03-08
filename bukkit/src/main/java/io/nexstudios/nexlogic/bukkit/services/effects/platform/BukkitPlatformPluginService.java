package io.nexstudios.nexlogic.bukkit.services.effects.platform;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.services.platform.PlatformPluginService;

import java.util.Objects;

public final class BukkitPlatformPluginService implements PlatformPluginService {

  private final String name;
  private final String version;

  public BukkitPlatformPluginService(PaperPluginService core) {
    Objects.requireNonNull(core, "core");
    var plugin = core.plugin();

    this.name = plugin.getPluginMeta().getName();
    this.version = plugin.getPluginMeta().getVersion();
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String version() {
    return version;
  }
}