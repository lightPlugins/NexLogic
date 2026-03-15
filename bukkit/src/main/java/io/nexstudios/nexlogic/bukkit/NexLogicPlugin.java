package io.nexstudios.nexlogic.bukkit;

import io.nexstudios.databaseservice.bukkit.DatabaseServiceModul;
import io.nexstudios.databaseservice.bukkit.service.api.DatabaseAsyncService;
import io.nexstudios.databaseservice.bukkit.service.api.DatabaseService;
import io.nexstudios.databaseservice.bukkit.service.api.HibernateEntityRegistryService;
import io.nexstudios.framework.paper.NexPaperPlugin;
import io.nexstudios.itemservice.bukkit.ItemServiceModule;
import io.nexstudios.menuservice.bukkit.service.menu.MenuServiceModule;
import io.nexstudios.nexlogic.bukkit.modules.BukkitRuntimeModule;
import io.nexstudios.nexlogic.bukkit.modules.CoreModule;
import io.nexstudios.nexlogic.bukkit.modules.EffectsModule;
import io.nexstudios.nexlogic.bukkit.modules.PlaceholderModule;
import io.nexstudios.nexlogic.bukkit.services.effects.bootstrap.TypeBuiltinService;
import io.nexstudios.nexlogic.bukkit.services.effects.listeners.*;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.async.AsyncExecutorService;
import io.nexstudios.nexlogic.bukkit.services.effects.command.LogicCommandService;
import io.nexstudios.nexlogic.bukkit.services.effects.reload.ReloadService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class NexLogicPlugin extends NexPaperPlugin {

  @Override
  protected void configureServices(@NotNull ServiceAccessor services) {

    // NexStudios Addon Services //
    // install DatabaseService
    services.install(new DatabaseServiceModul(this));
    initDatabase(services);
    // install ItemService
    services.install(new ItemServiceModule(this));
    // install MenuService
    services.install(new MenuServiceModule(this));
    // register Test Menus
    ExampleMainMenu.register(services);
    ExamplePagedMenu.register(services);
    ExampleControlsPaged.register(services);

    // install internal ServiceModules
    List<ServiceModule> modules = List.of(
        new CoreModule(),
        new PlaceholderModule(),
        new EffectsModule(),
        new BukkitRuntimeModule()
    );

    services.installAll(modules);
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

  private void initDatabase(ServiceAccessor services) {
    registerEntities(services.getService(HibernateEntityRegistryService.class));
    DatabaseService db = services.getService(DatabaseService.class);
    db.start();
    DatabaseAsyncService dbAsync = services.getService(DatabaseAsyncService.class);
    dbAsync.start();
  }

  private void registerEntities(HibernateEntityRegistryService reg) {

  }
}