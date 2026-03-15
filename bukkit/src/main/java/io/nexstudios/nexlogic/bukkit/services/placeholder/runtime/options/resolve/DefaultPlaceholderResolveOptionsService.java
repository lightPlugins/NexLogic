package io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.options.resolve;

import io.nexstudios.configservice.config.ConfigurationSection;
import io.nexstudios.configservice.config.FileConfiguration;
import io.nexstudios.configservice.service.singlereader.FileReaderService;
import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.services.placeholder.PlaceholderService;
import io.nexstudios.serviceregistry.di.Dependencies;

import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

@Dependencies({
    FileReaderService.class
})
public final class DefaultPlaceholderResolveOptionsService implements PlaceholderResolveOptionsService {

  private static final PlaceholderService.ResolveOptions FALLBACK = PlaceholderService.ResolveOptions.defaults();

  private final Logger logger;
  private final FileConfiguration cfg;

  private volatile PlaceholderService.ResolveOptions options = FALLBACK;

  public DefaultPlaceholderResolveOptionsService(PaperPluginService core) {
    Objects.requireNonNull(core, "core");

    FileReaderService reader = core.plugin().services().getService(FileReaderService.class);
    this.logger = core.plugin().getLogger();

    this.cfg = reader.load(
        Path.of("settings.yml"),
        "settings.yml",
        true
    );

    reload();
  }

  @Override
  public PlaceholderService.ResolveOptions options() {
    return options;
  }

  @Override
  public void reload() {
    try {
      cfg.reload();

      ConfigurationSection placeholder = cfg.getSection("placeholder");
      ConfigurationSection resolve = placeholder == null ? null : placeholder.getSection("resolve");

      int maxDepth = readInt(resolve, "max-depth", FALLBACK.maxDepth());
      int maxTokens = readInt(resolve, "max-tokens-per-input", FALLBACK.maxTokensPerInput());

      options = new PlaceholderService.ResolveOptions(maxDepth, maxTokens);
    } catch (Throwable t) {
      options = FALLBACK;
      logger.severe("Failed to reload placeholder resolve options, using defaults: " + t.getMessage());
      t.printStackTrace();
    }
  }

  private static int readInt(ConfigurationSection section, String key, int def) {
    if (section == null) return def;

    int v = section.getInt(key, Integer.MIN_VALUE);
    return v != Integer.MIN_VALUE ? v : def;
  }
}