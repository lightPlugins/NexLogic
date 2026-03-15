package io.nexstudios.nexlogic.bukkit.services.placeholder.loader;

import io.nexstudios.configservice.config.ConfigurationSection;
import io.nexstudios.configservice.config.FileConfiguration;
import io.nexstudios.configservice.service.multireader.MultiFileReaderService;
import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.placeholder.loader.PlaceholderFileModel;
import io.nexstudios.nexlogic.common.services.placeholder.loader.PlaceholderYamlLoaderService;
import io.nexstudios.serviceregistry.di.Dependencies;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Dependencies({
    MultiFileReaderService.class
})
public final class DefaultPlaceholderYamlLoaderService implements PlaceholderYamlLoaderService {

  private final MultiFileReaderService files;
  private final Logger logger;

  public DefaultPlaceholderYamlLoaderService(PaperPluginService core) {
    this.files = core.plugin().services().getService(MultiFileReaderService.class);
    this.logger = core.plugin().getLogger();
  }

  @Override
  public List<PlaceholderFileModel> loadAll() {
    Map<Path, FileConfiguration> map;
    try {
      map = files.loadAll(Path.of("placeholder"));
    } catch (Throwable t) {
      logger.severe("Failed to load placeholder directory: " + t.getMessage());
      t.printStackTrace();
      return List.of();
    }

    List<PlaceholderFileModel> out = new ArrayList<>(map.size());

    for (var entry : map.entrySet()) {
      Path rel = entry.getKey();
      FileConfiguration cfg = entry.getValue();
      if (cfg == null) continue;

      try {
        PlaceholderFileModel model = toModel(rel, cfg);
        if (model != null) out.add(model);
      } catch (Throwable t) {
        logger.severe("Failed to parse placeholder file '" + rel + "': " + t.getMessage());
        t.printStackTrace();
      }
    }

    return List.copyOf(out);
  }

  private PlaceholderFileModel toModel(Path rel, FileConfiguration cfg) {
    ConfigurationSection settings = cfg.getSection("settings");

    boolean enabled = settings == null || settings.getBoolean("enable", true);

    String identifier = settings == null ? "" : settings.getString("identifier", "");
    identifier = identifier == null ? "" : identifier.trim().toLowerCase();

    if (identifier.isBlank()) {
      logger.severe("Placeholder file '" + rel + "' is missing settings.identifier");
      return null;
    }

    Duration defaultTtl = Duration.ofMillis(settings == null ? 0L : settings.getInt("ttl-millis", 0));

    String owner = "placeholders:" + ownerIdFromRelative(rel);

    List<PlaceholderFileModel.Entry> placeholders = new ArrayList<>();
    for (ConfigurationSection e : cfg.getSectionList("placeholders")) {
      if (e == null) continue;

      String id = e.getString("id", "");
      id = id == null ? "" : id.trim().toLowerCase();

      if (id.isBlank()) {
        logger.severe("Placeholder file '" + rel + "' has an entry with missing id");
        continue;
      }

      String value = e.getString("value", "");
      Duration ttlOverride = e.contains("ttl-millis") ? Duration.ofMillis(e.getInt("ttl-millis", 0)) : null;

      placeholders.add(new PlaceholderFileModel.Entry(id, value, ttlOverride));
    }

    return new PlaceholderFileModel(
        owner,
        identifier,
        enabled,
        defaultTtl,
        List.copyOf(placeholders)
    );
  }

  private static String ownerIdFromRelative(Path rel) {
    String s = (rel == null ? "unknown.yml" : rel.toString()).replace('\\', '/').toLowerCase();

    if (s.endsWith(".yml")) s = s.substring(0, s.length() - ".yml".length());

    // Normalize path separators into a stable owner id
    s = s.replaceAll("/+", "/");
    s = s.replaceAll("[^a-z0-9/_\\-.]", "_");

    if (s.startsWith("/")) s = s.substring(1);
    if (s.isBlank()) s = "unknown";

    return s;
  }
}