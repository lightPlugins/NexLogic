package io.nexstudios.nexlogic.bukkit;

import io.nexstudios.framework.paper.NexPaperPlugin;
import io.nexstudios.nexlogic.bukkit.services.blocks.BlockKeyService;
import io.nexstudios.nexlogic.bukkit.services.blocks.DefaultBlockKeyService;
import io.nexstudios.nexlogic.bukkit.services.bootstrap.BukkitBuiltinsService;
import io.nexstudios.nexlogic.bukkit.services.logging.BukkitLoggerService;
import io.nexstudios.nexlogic.bukkit.services.platform.BukkitPlatformPluginService;
import io.nexstudios.nexlogic.bukkit.types.trigger.BreakBlockTriggerType;
import io.nexstudios.nexlogic.common.services.engine.DefaultLogicEngineService;
import io.nexstudios.nexlogic.common.services.engine.LogicEngineService;
import io.nexstudios.nexlogic.bukkit.services.executor.async.AsyncExecutorService;
import io.nexstudios.nexlogic.bukkit.services.executor.async.DefaultAsyncExecutorService;
import io.nexstudios.nexlogic.bukkit.services.config.ConfigPathService;
import io.nexstudios.nexlogic.bukkit.services.config.DefaultConfigPathService;
import io.nexstudios.nexlogic.bukkit.services.context.BukkitContextResolverService;
import io.nexstudios.nexlogic.bukkit.services.context.DefaultBukkitContextResolverService;
import io.nexstudios.nexlogic.bukkit.types.trigger.JoinTriggerType;
import io.nexstudios.nexlogic.bukkit.services.executor.DefaultMainThreadExecutorService;
import io.nexstudios.nexlogic.bukkit.services.executor.MainThreadExecutorService;
import io.nexstudios.nexlogic.bukkit.services.command.NexLogicCommandService;
import io.nexstudios.nexlogic.bukkit.services.reload.DefaultReloadService;
import io.nexstudios.nexlogic.bukkit.services.reload.ReloadService;
import io.nexstudios.nexlogic.bukkit.services.loader.DefaultYamlLoaderService;
import io.nexstudios.nexlogic.bukkit.services.loader.YamlLoaderService;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
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
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public final class NexLogicPlugin extends NexPaperPlugin {

  @Override
  protected void configureServices(@NotNull ServiceAccessor services) {
    // Core registries and runtime
    services.register(LoggerService.class, BukkitLoggerService.class);
    services.register(PlatformPluginService.class, BukkitPlatformPluginService.class);
    services.register(BlockKeyService.class, DefaultBlockKeyService.class);
    services.register(EffectTypeRegistryService.class, DefaultEffectTypeRegistryService.class);
    services.register(ConditionTypeRegistryService.class, DefaultConditionTypeRegistryService.class);
    services.register(AddonRegistryService.class, DefaultAddonRegistryService.class);

    // Trigger schema MUST exist before FilterService is instantiated
    services.register(TriggerContextSchemaService.class, DefaultTriggerContextSchemaService.class);

    // Filters MUST be registered before services that depend on FilterService (e.g. TriggerRuleRegistry)
    services.register(FilterTypeRegistryService.class, DefaultFilterTypeRegistryService.class);
    services.register(FilterService.class, DefaultFilterService.class);

    // Built-in Bukkit registrations (effects/conditions/filters)
    services.register(BukkitBuiltinsService.class, BukkitBuiltinsService.class);
    services.register(ActionRuntimeService.class, DefaultActionRuntimeService.class);
    services.register(TriggerRuntimeService.class, DefaultTriggerRuntimeService.class);
    services.register(TriggerRegistrationService.class, DefaultTriggerRegistrationService.class);
    services.register(TriggerBusService.class, DefaultTriggerBusService.class);
    services.register(TriggerRuleRegistryService.class, DefaultTriggerRuleRegistryService.class);
    services.register(LogicEngineService.class, DefaultLogicEngineService.class);

    // Bukkit-specific
    services.register(BukkitContextResolverService.class, DefaultBukkitContextResolverService.class);
    services.register(MainThreadExecutorService.class, DefaultMainThreadExecutorService.class);
    services.register(AsyncExecutorService.class, DefaultAsyncExecutorService.class);
    services.register(ConfigPathService.class, DefaultConfigPathService.class);
    services.register(YamlLoaderService.class, DefaultYamlLoaderService.class);
    services.register(ReloadService.class, DefaultReloadService.class);

    // Commands and triggers are services too
    services.register(NexLogicCommandService.class, NexLogicCommandService.class);
    services.register(JoinTriggerType.class, JoinTriggerType.class);
    services.register(BreakBlockTriggerType.class, BreakBlockTriggerType.class);
  }

  @Override
  protected void load() {
    getLogger().info("NexLogic is loading...");
  }

  @Override
  protected void start() {
    getLogger().info("NexLogic is starting...");

    // Register commands via NexFramework command system
    registerCommands(NexLogicCommandService.class);

    // Register listeners
    var join = services().getService(JoinTriggerType.class);
    var breakBlock = services().getService(BreakBlockTriggerType.class);
    Bukkit.getPluginManager().registerEvents(join, this);
    Bukkit.getPluginManager().registerEvents(breakBlock, this);

    // Register built-in Bukkit types (conditions/effects/filters)
    services().getService(BukkitBuiltinsService.class).registerAll();

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