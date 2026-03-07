package io.nexstudios.nexlogic.bukkit.services.blocks;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DefaultBlockKeyService implements BlockKeyService {

  @FunctionalInterface
  public interface Provider {
    Optional<String> keyOf(Block block);
  }

  private final List<Provider> providers = new ArrayList<>();

  public DefaultBlockKeyService(PaperPluginService core) {
    Objects.requireNonNull(core, "core");

    // Highest priority providers first (future: Nexo provider, etc.)
    // providers.add(new NexoBlockProvider(core));

    // Fallback: vanilla Bukkit material key -> "minecraft:stone"
    providers.add(block -> Optional.ofNullable(block)
        .map(Block::getType)
        .map(m -> m.getKey().toString())
    );
  }

  @Override
  public String keyOf(Block block) {
    for (Provider p : providers) {
      String k = p.keyOf(block).orElse(null);
      if (k != null && !k.isBlank()) return k.toLowerCase();
    }
    // should never happen with fallback, but stay safe:
    return "minecraft:air";
  }

  @Override
  public String normalize(String id) {
    if (id == null) return "";
    String s = id.trim().toLowerCase();
    if (s.isEmpty()) return s;

    // allow shorthand like "stone" -> "minecraft:stone"
    if (!s.contains(":")) return "minecraft:" + s;

    return s;
  }
}