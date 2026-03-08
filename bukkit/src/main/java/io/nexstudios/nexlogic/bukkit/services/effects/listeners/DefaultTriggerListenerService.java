package io.nexstudios.nexlogic.bukkit.services.effects.listeners;

import io.nexstudios.framework.paper.NexPaperPlugin;
import io.nexstudios.framework.paper.services.ServiceListener;
import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.bukkit.effects.trigger.BlockBreakTriggerType;
import io.nexstudios.nexlogic.bukkit.effects.trigger.PlayerJoinTriggerType;
import io.nexstudios.nexlogic.bukkit.effects.trigger.BlockPlaceTriggerType;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Objects;

/**
 * Registers Bukkit event listeners for built-in trigger types.
 */
@Dependencies({
    PlayerJoinTriggerType.class,
    BlockBreakTriggerType.class,
    BlockPlaceTriggerType.class
})
public final class DefaultTriggerListenerService implements TriggerListenerService {

  private final NexPaperPlugin plugin;
  private final ServiceAccessor services;

  /**
   * A list of listener types that represent the built-in trigger types for events in the system.
   * These types extend the {@link ServiceListener} interface and define specific behavior for handling
   * Bukkit events such as player joining and block breaking.
   */
  private final List<Class<? extends ServiceListener>> listenerTypes = List.of(
      PlayerJoinTriggerType.class,
      BlockBreakTriggerType.class,
      BlockPlaceTriggerType.class
  );

  public DefaultTriggerListenerService(PaperPluginService core) {
    Objects.requireNonNull(core, "core");
    this.plugin = core.plugin();
    this.services = plugin.services();
  }

  @Override
  public void registerAll() {
    for (Class<? extends ServiceListener> type : listenerTypes) {
      ServiceListener listener = services.getService(type);
      Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
  }
}