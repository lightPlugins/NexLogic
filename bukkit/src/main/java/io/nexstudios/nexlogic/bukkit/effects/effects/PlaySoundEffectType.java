package io.nexstudios.nexlogic.bukkit.effects.effects;

import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextKeys;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.MainThreadExecutorService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.runtime.EffectInstance;
import io.nexstudios.nexlogic.common.effects.types.EffectTypeService;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Dependencies({
    MainThreadExecutorService.class,
    LoggerService.class
})
public final class PlaySoundEffectType implements EffectTypeService {

  private final MainThreadExecutorService mainThread;
  private final LoggerService loggerService;

  public PlaySoundEffectType(ServiceAccessor accessor) {
    this.mainThread = accessor.getService(MainThreadExecutorService.class);
    this.loggerService = accessor.getService(LoggerService.class);
  }

  @Override
  public String id() {
    return "play-sound";
  }

  @Override
  public Set<ContextCapability> requiredCapabilities() {
    return Set.of(ContextCapability.PLAYER);
  }

  @Override
  public EffectInstance create(ConfigSection args) {
    final String rawSound = args == null ? "" : args.getString("sound", "");
    final double volume = args == null ? 1.0 : args.getDouble("volume", 1.0);
    final double pitch = args == null ? 1.0 : args.getDouble("pitch", 1.0);

    final Sound sound = resolveSound(rawSound).orElse(null);
    if (sound == null) {
      loggerService.logger().severe("Invalid sound for effect 'play-sound': " + rawSound);
      return ctx -> { /* no-op */ };
    }

    final float volumeF = (float) volume;
    final float pitchF = (float) pitch;

    return ctx -> {
      if (ctx == null) return;

      Player player = ctx.get(BukkitContextKeys.PLAYER).orElse(null);
      if (player == null || !player.isOnline()) return;

      Runnable task = () -> {
        if (!player.isOnline()) return;
        player.playSound(player.getLocation(), sound, volumeF, pitchF);
      };

      if (mainThread.isMainThread()) task.run();
      else mainThread.execute(task);
    };
  }

  private static Optional<Sound> resolveSound(String rawSound) {
    if (rawSound == null) return Optional.empty();

    String normalized = rawSound.trim().toLowerCase(Locale.ROOT);
    if (normalized.isBlank()) return Optional.empty();

    if (!normalized.contains(":")) {
      normalized = "minecraft:" + normalized;
    }

    NamespacedKey key = NamespacedKey.fromString(normalized);
    if (key == null) return Optional.empty();

    try {
      return Optional.ofNullable(Registry.SOUNDS.get(key));
    } catch (Throwable ignored) {
      return Optional.empty();
    }
  }
}

