package io.nexstudios.nexlogic.bukkit;

import io.nexstudios.framework.paper.NexPaperPlugin;
import io.nexstudios.nexlogic.bukkit.effects.trigger.BlockPlaceTriggerType;
import io.nexstudios.nexlogic.bukkit.services.effects.blocks.BlockKeyService;
import io.nexstudios.nexlogic.bukkit.services.effects.blocks.DefaultBlockKeyService;
import io.nexstudios.nexlogic.bukkit.services.effects.blocks.PlayerPlacedBlockTrackerService;
import io.nexstudios.nexlogic.bukkit.services.effects.bootstrap.TypeBuiltinService;
import io.nexstudios.nexlogic.bukkit.services.effects.listeners.*;
import io.nexstudios.nexlogic.bukkit.services.effects.logging.BukkitLoggerService;
import io.nexstudios.nexlogic.bukkit.services.effects.platform.BukkitPlatformPluginService;
import io.nexstudios.nexlogic.bukkit.effects.trigger.BlockBreakTriggerType;
import io.nexstudios.nexlogic.bukkit.services.placeholder.DefaultPlaceholderReloadService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.PlaceholderReloadService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.loader.DefaultPlaceholderYamlLoaderService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.DefaultPlaceholderRuntimeService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.PlaceholderRuntimeService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.options.cache.DefaultPlaceholderCacheOptionsService;
import io.nexstudios.nexlogic.common.services.placeholder.cache.PlaceholderCacheOptionsService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.options.resolve.DefaultPlaceholderResolveOptionsService;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.options.resolve.PlaceholderResolveOptionsService;
import io.nexstudios.nexlogic.common.services.engine.DefaultLogicEngineService;
import io.nexstudios.nexlogic.common.services.engine.LogicEngineService;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.async.AsyncExecutorService;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.async.DefaultAsyncExecutorService;
import io.nexstudios.nexlogic.bukkit.services.effects.config.ConfigPathService;
import io.nexstudios.nexlogic.bukkit.services.effects.config.DefaultConfigPathService;
import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextResolverService;
import io.nexstudios.nexlogic.bukkit.services.effects.context.DefaultBukkitContextResolverService;
import io.nexstudios.nexlogic.bukkit.effects.trigger.PlayerJoinTriggerType;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.DefaultMainThreadExecutorService;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.MainThreadExecutorService;
import io.nexstudios.nexlogic.bukkit.services.effects.command.LogicCommandService;
import io.nexstudios.nexlogic.bukkit.services.effects.reload.DefaultReloadService;
import io.nexstudios.nexlogic.bukkit.services.effects.reload.ReloadService;
import io.nexstudios.nexlogic.bukkit.services.effects.loader.DefaultYamlLoaderService;
import io.nexstudios.nexlogic.bukkit.services.effects.loader.YamlLoaderService;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.nexlogic.common.services.placeholder.DefaultPlaceholderService;
import io.nexstudios.nexlogic.common.services.placeholder.PlaceholderService;
import io.nexstudios.nexlogic.common.services.placeholder.loader.PlaceholderYamlLoaderService;
import io.nexstudios.nexlogic.common.services.platform.PlatformPluginService;
import io.nexstudios.nexlogic.common.services.runtime.ActionRuntimeService;
import io.nexstudios.nexlogic.common.services.runtime.DefaultActionRuntimeService;
import io.nexstudios.nexlogic.common.services.registry.addon.AddonRegistryService;
import io.nexstudios.nexlogic.common.services.registry.addon.DefaultAddonRegistryService;
import io.nexstudios.nexlogic.common.services.registry.condition.ConditionTypeRegistryService;
import io.nexstudios.nexlogic.common.services.registry.condition.DefaultConditionTypeRegistryService;
import io.nexstudios.nexlogic.common.services.registry.effect.DefaultEffectTypeRegistryService;
import io.nexstudios.nexlogic.common.services.registry.effect.EffectTypeRegistryService;
import io.nexstudios.nexlogic.common.services.filters.DefaultFilterService;
import io.nexstudios.nexlogic.common.services.filters.FilterService;
import io.nexstudios.nexlogic.common.services.registry.filter.DefaultFilterTypeRegistryService;
import io.nexstudios.nexlogic.common.services.registry.filter.FilterTypeRegistryService;
import io.nexstudios.nexlogic.common.services.triggers.register.DefaultTriggerRegistrationService;
import io.nexstudios.nexlogic.common.services.triggers.register.TriggerRegistrationService;
import io.nexstudios.nexlogic.common.services.triggers.schema.DefaultTriggerContextSchemaService;
import io.nexstudios.nexlogic.common.services.triggers.schema.TriggerContextSchemaService;
import io.nexstudios.nexlogic.common.services.triggers.bus.DefaultTriggerBusService;
import io.nexstudios.nexlogic.common.services.triggers.bus.TriggerBusService;
import io.nexstudios.nexlogic.common.services.triggers.rules.DefaultTriggerRuleRegistryService;
import io.nexstudios.nexlogic.common.services.triggers.rules.TriggerRuleRegistryService;
import io.nexstudios.nexlogic.common.services.triggers.runtime.DefaultTriggerRuntimeService;
import io.nexstudios.nexlogic.common.services.triggers.runtime.TriggerRuntimeService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.jetbrains.annotations.NotNull;

public final class NexLogicPlugin extends NexPaperPlugin {

  @Override
  protected void configureServices(@NotNull ServiceAccessor services) {
    // Core registries and runtime
    services.register(LoggerService.class, BukkitLoggerService.class);
    services.register(PlatformPluginService.class, BukkitPlatformPluginService.class);
    services.register(BlockKeyService.class, DefaultBlockKeyService.class);
    // Placeholders
    services.register(PlaceholderYamlLoaderService.class, DefaultPlaceholderYamlLoaderService.class);
    services.register(PlaceholderResolveOptionsService.class, DefaultPlaceholderResolveOptionsService.class);
    services.register(PlaceholderCacheOptionsService.class, DefaultPlaceholderCacheOptionsService.class);
    services.register(PlaceholderService.class, DefaultPlaceholderService.class);
    services.register(PlaceholderReloadService.class, DefaultPlaceholderReloadService.class);
    services.register(PlaceholderRuntimeService.class, DefaultPlaceholderRuntimeService.class);
    // mvp registries
    services.register(EffectTypeRegistryService.class, DefaultEffectTypeRegistryService.class);
    services.register(ConditionTypeRegistryService.class, DefaultConditionTypeRegistryService.class);
    services.register(AddonRegistryService.class, DefaultAddonRegistryService.class);
    // Trigger schema MUST exist before FilterService is instantiated
    services.register(TriggerContextSchemaService.class, DefaultTriggerContextSchemaService.class);
    // Filters MUST be registered before services that depend on FilterService (e.g. TriggerRuleRegistry)
    services.register(FilterTypeRegistryService.class, DefaultFilterTypeRegistryService.class);
    services.register(FilterService.class, DefaultFilterService.class);
    // Built-in Bukkit registrations (effects/conditions/filters)
    services.register(TypeBuiltinService.class, TypeBuiltinService.class);
    services.register(ActionRuntimeService.class, DefaultActionRuntimeService.class);
    services.register(TriggerRuntimeService.class, DefaultTriggerRuntimeService.class);
    services.register(TriggerRegistrationService.class, DefaultTriggerRegistrationService.class);
    services.register(TriggerBusService.class, DefaultTriggerBusService.class);
    services.register(TriggerRuleRegistryService.class, DefaultTriggerRuleRegistryService.class);
    services.register(LogicEngineService.class, DefaultLogicEngineService.class);
    // Bukkit-specific
    services.register(BukkitContextResolverService.class, DefaultBukkitContextResolverService.class);
    services.register(PlayerPlacedBlockTrackerService.class, PlayerPlacedBlockTrackerService.class);
    services.register(MainThreadExecutorService.class, DefaultMainThreadExecutorService.class);
    services.register(AsyncExecutorService.class, DefaultAsyncExecutorService.class);
    services.register(ConfigPathService.class, DefaultConfigPathService.class);
    services.register(YamlLoaderService.class, DefaultYamlLoaderService.class);
    services.register(ReloadService.class, DefaultReloadService.class);
    // Commands
    services.register(LogicCommandService.class, LogicCommandService.class);
    // triggers
    services.register(PlayerJoinTriggerType.class, PlayerJoinTriggerType.class);
    services.register(BlockBreakTriggerType.class, BlockBreakTriggerType.class);
    services.register(BlockPlaceTriggerType.class, BlockPlaceTriggerType.class);
    // Player-placed maintenance listeners
    services.register(PistonMoveListener.class, PistonMoveListener.class);
    services.register(ExplosionCleanupListener.class, ExplosionCleanupListener.class);
    services.register(BlockTransformCleanupListener.class, BlockTransformCleanupListener.class);
    services.register(FallingBlockCleanupListener.class, FallingBlockCleanupListener.class);
    // Trigger listener registration
    services.register(TriggerListenerService.class, DefaultTriggerListenerService.class);
  }

  @Override
  protected void load() {
    getLogger().info("NexLogic is loading...");
  }

  @Override
  protected void start() {
    getLogger().info("NexLogic is starting...");

    // Register commands via NexFramework command system
    registerCommands(LogicCommandService.class);

    // Register trigger listeners
    services().getService(TriggerListenerService.class).registerAll();

    // Register built-in Bukkit types (conditions/effects/filters)
    services().getService(TypeBuiltinService.class).registerAll();

    // Initial load/compile async, then swap automatically
    services().getService(ReloadService.class).reloadAsync();

    getLogger().info("NexLogic started.");
  }

  @Override
  protected void stop() {
    services().getService(AsyncExecutorService.class).shutdown();
    getLogger().info("NexLogic is stopping...");
  }
}