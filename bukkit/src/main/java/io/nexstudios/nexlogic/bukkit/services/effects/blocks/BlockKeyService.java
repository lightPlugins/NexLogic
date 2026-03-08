package io.nexstudios.nexlogic.bukkit.services.effects.blocks;

import io.nexstudios.serviceregistry.di.Service;
import org.bukkit.block.Block;

import java.util.Set;

public interface BlockKeyService extends Service {

  /**
   * @return canonical key for a block, e.g. "minecraft:stone" or "nexo:my_block".
   *         Must be lowercase.
   */
  String keyOf(Block block);

  /**
   * Normalizes a configured id. Accepts "stone" or "minecraft:stone".
   */
  String normalize(String id);

  /**
   * Convenience matching helper.
   */
  default boolean matches(Block block, Set<String> allowedKeysNormalized) {
    if (block == null) return false;
    if (allowedKeysNormalized == null || allowedKeysNormalized.isEmpty()) return true;
    return allowedKeysNormalized.contains(keyOf(block));
  }
}