package io.nexstudios.nexlogic.bukkit.modules;

import io.nexstudios.nexlogic.bukkit.services.effects.blocks.PlayerPlacedBlockTrackerService;
import io.nexstudios.nexlogic.bukkit.services.effects.bootstrap.TypeBuiltinService;
import io.nexstudios.nexlogic.bukkit.services.command.LogicCommandService;
import io.nexstudios.nexlogic.bukkit.services.effects.config.ConfigPathService;
import io.nexstudios.nexlogic.bukkit.services.effects.config.DefaultConfigPathService;
import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextResolverService;
import io.nexstudios.nexlogic.bukkit.services.effects.context.DefaultBukkitContextResolverService;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.DefaultMainThreadExecutorService;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.MainThreadExecutorService;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.async.AsyncExecutorService;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.async.DefaultAsyncExecutorService;
import io.nexstudios.nexlogic.bukkit.services.effects.listeners.BlockTransformCleanupListener;
import io.nexstudios.nexlogic.bukkit.services.effects.listeners.DefaultTriggerListenerService;
import io.nexstudios.nexlogic.bukkit.services.effects.listeners.ExplosionCleanupListener;
import io.nexstudios.nexlogic.bukkit.services.effects.listeners.FallingBlockCleanupListener;
import io.nexstudios.nexlogic.bukkit.services.effects.listeners.PistonMoveListener;
import io.nexstudios.nexlogic.bukkit.services.effects.listeners.TriggerListenerService;
import io.nexstudios.nexlogic.bukkit.services.effects.loader.DefaultYamlLoaderService;
import io.nexstudios.nexlogic.bukkit.services.effects.loader.YamlLoaderService;
import io.nexstudios.nexlogic.bukkit.services.effects.reload.DefaultReloadService;
import io.nexstudios.nexlogic.bukkit.services.effects.reload.ReloadService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;

public final class BukkitRuntimeServiceModule implements ServiceModule {

  @Override
  public void install(ServiceAccessor services) {
    // Built-in Bukkit registrations (effects/conditions/filters)
    services.register(TypeBuiltinService.class, TypeBuiltinService.class);

    // Bukkit-specific runtime services
    services.register(BukkitContextResolverService.class, DefaultBukkitContextResolverService.class);
    services.register(PlayerPlacedBlockTrackerService.class, PlayerPlacedBlockTrackerService.class);

    services.register(MainThreadExecutorService.class, DefaultMainThreadExecutorService.class);
    services.register(AsyncExecutorService.class, DefaultAsyncExecutorService.class);

    services.register(ConfigPathService.class, DefaultConfigPathService.class);
    services.register(YamlLoaderService.class, DefaultYamlLoaderService.class);
    services.register(ReloadService.class, DefaultReloadService.class);

    // Commands
    services.register(LogicCommandService.class, LogicCommandService.class);

    // Maintenance listeners
    services.register(PistonMoveListener.class, PistonMoveListener.class);
    services.register(ExplosionCleanupListener.class, ExplosionCleanupListener.class);
    services.register(BlockTransformCleanupListener.class, BlockTransformCleanupListener.class);
    services.register(FallingBlockCleanupListener.class, FallingBlockCleanupListener.class);

    // Trigger listener registration
    services.register(TriggerListenerService.class, DefaultTriggerListenerService.class);
  }
}