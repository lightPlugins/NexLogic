package io.nexstudios.nexlogic.bukkit.services.listeners;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.bukkit.types.trigger.BreakBlockTriggerType;
import io.nexstudios.nexlogic.bukkit.types.trigger.JoinTriggerType;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/**
 * Default implementation of the {@link TriggerListenerService} for registering
 * Bukkit event listeners. This service binds predefined trigger types (e.g.,
 * {@link JoinTriggerType}, {@link BreakBlockTriggerType}) to the Bukkit plugin's
 * event system, enabling custom trigger-based logic execution.
 */
@Dependencies({
    JoinTriggerType.class,
    BreakBlockTriggerType.class
})
public final class DefaultTriggerListenerService implements TriggerListenerService {

  private final Plugin plugin;
  private final ServiceAccessor services;

  public DefaultTriggerListenerService(PaperPluginService core) {
    Objects.requireNonNull(core, "core");
    this.plugin = core.plugin();
    this.services = core.plugin().services();
  }

  @Override
  public void registerAll() {
    var join = services.getService(JoinTriggerType.class);
    var breakBlock = services.getService(BreakBlockTriggerType.class);

    Bukkit.getPluginManager().registerEvents(join, plugin);
    Bukkit.getPluginManager().registerEvents(breakBlock, plugin);
  }
}