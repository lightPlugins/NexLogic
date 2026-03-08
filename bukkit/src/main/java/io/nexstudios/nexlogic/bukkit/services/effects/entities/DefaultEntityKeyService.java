package io.nexstudios.nexlogic.bukkit.services.effects.entities;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DefaultEntityKeyService implements EntityKeyService {


  @FunctionalInterface
  public interface Provider {
    Optional<String> keyOf(Entity entity);
  }

  private final List<DefaultEntityKeyService.Provider> providers = new ArrayList<>();

  public DefaultEntityKeyService(PaperPluginService core) {
    Objects.requireNonNull(core, "core");

    providers.add(block -> Optional.ofNullable(block)
        .map(Entity::getType)
        .map(m -> m.getKey().toString())
    );
  }

  @Override
  public String keyOf(Entity entity) {
    for (DefaultEntityKeyService.Provider p : providers) {
      String k = p.keyOf(entity).orElse(null);
      if (k != null && !k.isBlank()) return k.toLowerCase();
    }
    // should never happen with fallback, but stay safe:
    return "minecraft:unknown";
  }

  @Override
  public String normalize(String id) {
    if (id == null) return "";
    String s = id.trim().toLowerCase();
    if (s.isEmpty()) return s;

    // allow shorthand like "pig" -> "minecraft:pig"
    if (!s.contains(":")) return "minecraft:" + s;

    return s;
  }
}
