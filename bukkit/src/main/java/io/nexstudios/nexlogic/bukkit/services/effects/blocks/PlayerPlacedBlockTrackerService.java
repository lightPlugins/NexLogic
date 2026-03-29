package io.nexstudios.nexlogic.bukkit.services.effects.blocks;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.serviceregistry.di.Service;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

/**
 * Tracks player-placed blocks per chunk using Chunk PersistentDataContainer.
 *
 * Storage format:
 * - key: nexlogic:player_placed_blocks
 * - type: LONG_ARRAY
 * - value: packed block positions within the chunk (x,z in [0..15], y in [0..4095])
 */
public final class PlayerPlacedBlockTrackerService implements Service {

  private static final String PDC_KEY = "player_placed_blocks";

  private final NamespacedKey key;

  public PlayerPlacedBlockTrackerService(PaperPluginService core) {
    this.key = new NamespacedKey(core.plugin(), PDC_KEY);
  }

  public void markPlaced(Block block) {
    if (block == null) return;

    Chunk chunk = block.getChunk();
    PersistentDataContainer pdc = chunk.getPersistentDataContainer();

    long packed = pack(block);

    long[] existing = pdc.get(key, PersistentDataType.LONG_ARRAY);
    long[] out = addUnique(existing, packed);

    if (out == existing) return;
    pdc.set(key, PersistentDataType.LONG_ARRAY, out);
  }

  public boolean isPlayerPlaced(Block block) {
    if (block == null) return false;

    PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
    long[] existing = pdc.get(key, PersistentDataType.LONG_ARRAY);
    if (existing == null || existing.length == 0) return false;

    long packed = pack(block);
    for (long v : existing) {
      if (v == packed) return true;
    }
    return false;
  }

  public void unmark(Block block) {
    if (block == null) return;

    Chunk chunk = block.getChunk();
    PersistentDataContainer pdc = chunk.getPersistentDataContainer();

    long[] existing = pdc.get(key, PersistentDataType.LONG_ARRAY);
    if (existing == null || existing.length == 0) return;

    long packed = pack(block);
    long[] out = remove(existing, packed);

    if (out == existing) return; // nothing removed

    if (out.length == 0) {
      pdc.remove(key);
      return;
    }

    pdc.set(key, PersistentDataType.LONG_ARRAY, out);
  }

  /**
   * Moves a player-placed marker from one block position to another.
   * If the source isn't marked, this is a no-op.
   *
   * Handles cross-chunk movement correctly.
   */
  public void move(Block from, Block to) {
    if (from == null || to == null) return;

    Chunk fromChunk = from.getChunk();
    Chunk toChunk = to.getChunk();

    long fromPacked = pack(from);
    long toPacked = pack(to);

    if (fromChunk.getX() == toChunk.getX() && fromChunk.getZ() == toChunk.getZ() && fromChunk.getWorld().equals(toChunk.getWorld())) {
      // same chunk: update single PDC array
      PersistentDataContainer pdc = fromChunk.getPersistentDataContainer();
      long[] existing = pdc.get(key, PersistentDataType.LONG_ARRAY);
      if (existing == null || existing.length == 0) return;

      if (!contains(existing, fromPacked)) return;

      long[] removed = remove(existing, fromPacked);
      long[] added = addUnique(removed, toPacked);

      if (added.length == 0) pdc.remove(key);
      else pdc.set(key, PersistentDataType.LONG_ARRAY, added);

      return;
    }

    // cross-chunk: remove from source chunk, add to target chunk
    PersistentDataContainer fromPdc = fromChunk.getPersistentDataContainer();
    long[] fromExisting = fromPdc.get(key, PersistentDataType.LONG_ARRAY);
    if (fromExisting == null || fromExisting.length == 0) return;

    if (!contains(fromExisting, fromPacked)) return;

    long[] fromOut = remove(fromExisting, fromPacked);
    if (fromOut.length == 0) fromPdc.remove(key);
    else fromPdc.set(key, PersistentDataType.LONG_ARRAY, fromOut);

    PersistentDataContainer toPdc = toChunk.getPersistentDataContainer();
    long[] toExisting = toPdc.get(key, PersistentDataType.LONG_ARRAY);
    long[] toOut = addUnique(toExisting, toPacked);
    if (toOut != toExisting) toPdc.set(key, PersistentDataType.LONG_ARRAY, toOut);
  }

  private static boolean contains(long[] arr, long v) {
    if (arr == null) return false;
    for (long x : arr) if (x == v) return true;
    return false;
  }

  private static long[] addUnique(long[] existing, long v) {
    if (existing == null || existing.length == 0) return new long[]{v};
    for (long x : existing) if (x == v) return existing;
    long[] out = Arrays.copyOf(existing, existing.length + 1);
    out[out.length - 1] = v;
    return out;
  }

  private static long[] remove(long[] existing, long v) {
    if (existing == null || existing.length == 0) return existing;

    int idx = -1;
    for (int i = 0; i < existing.length; i++) {
      if (existing[i] == v) {
        idx = i;
        break;
      }
    }
    if (idx < 0) return existing;

    if (existing.length == 1) return new long[0];

    long[] out = new long[existing.length - 1];
    System.arraycopy(existing, 0, out, 0, idx);
    System.arraycopy(existing, idx + 1, out, idx, existing.length - idx - 1);
    return out;
  }

  private static long pack(Block block) {
    int x = block.getX() & 0xF; // 0..15
    int z = block.getZ() & 0xF; // 0..15
    int y = block.getY();

    // keep it bounded for packing
    if (y < 0) y = 0;
    if (y > 4095) y = 4095;

    // bits: x(4) | z(4) | y(12) => 20 bits total
    return (x & 0xFL) | ((z & 0xFL) << 4) | (((long) y & 0xFFFL) << 8);
  }
}