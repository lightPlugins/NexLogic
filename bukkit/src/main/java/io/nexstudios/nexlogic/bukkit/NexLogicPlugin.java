package io.nexstudios.nexlogic.bukkit;

import io.nexstudios.commandservice.CommandServiceModule;
import io.nexstudios.commandservice.service.commands.CommandService;
import io.nexstudios.configservice.ConfigServiceModule;
import io.nexstudios.databaseservice.bukkit.DatabaseServiceModul;
import io.nexstudios.databaseservice.bukkit.service.api.DatabaseAsyncService;
import io.nexstudios.databaseservice.bukkit.service.api.DatabaseService;
import io.nexstudios.databaseservice.bukkit.service.api.HibernateMappingContributionService;
import io.nexstudios.databaseservice.bukkit.service.api.pubsub.RedisPubSubService;
import io.nexstudios.framework.paper.NexPaperPlugin;
import io.nexstudios.itemservice.bukkit.ItemServiceModule;
import io.nexstudios.languageservice.LanguageServiceModule;
import io.nexstudios.menuservice.bukkit.service.menu.MenuServiceModule;
import io.nexstudios.nexlogic.bukkit.modules.*;
import io.nexstudios.nexlogic.bukkit.services.entity.EconomyBalanceEntity;
import io.nexstudios.nexlogic.bukkit.services.effects.bootstrap.TypeBuiltinService;
import io.nexstudios.nexlogic.bukkit.services.effects.listeners.*;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.async.AsyncExecutorService;
import io.nexstudios.nexlogic.bukkit.services.effects.command.LogicCommandService;
import io.nexstudios.nexlogic.bukkit.services.effects.reload.ReloadService;
import io.nexstudios.nexlogic.bukkit.services.entity.nexeconomy.*;
import io.nexstudios.nexlogic.common.services.engine.DefaultLogicEngineService;
import io.nexstudios.nexlogic.common.services.engine.LogicEngineService;
import io.nexstudios.nexlogic.common.services.triggers.bus.TriggerBusService;
import io.nexstudios.nexlogic.common.services.triggers.register.DefaultTriggerRegistrationService;
import io.nexstudios.nexlogic.common.services.triggers.register.TriggerRegistrationService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class NexLogicPlugin extends NexPaperPlugin {

  @Override
  protected void configureServices(@NotNull ServiceAccessor services) {

    // NexStudios Addon Service Modules //
    // install ConfigService
    services.install(new ConfigServiceModule(getDataPath(), getClassLoader()));
    // install LanguageService (require ConfigService loaded)
    services.install(new LanguageServiceModule(this));
    // install DatabaseService
    services.install(new DatabaseServiceModul(this));
    // install ItemService
    services.install(new ItemServiceModule(this));
    // install Command Service
    services.install(new CommandServiceModule(this));

    // install internal ServiceModules
    List<ServiceModule> modules = List.of(
        new CoreModule(),
        new PlaceholderModule(),
        new EffectsModule(),
        new BukkitRuntimeModule(),
        new UtilityModule()
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
    services().install(new HookServiceModule());
    services().install(new MenuServiceModule(this));
    initDatabase(services());

    services().getService(CommandService.class).registerAll(
        List.of(
            LogicCommandService.class
        )
    );

    ExampleMainMenu.register(services());
    ExamplePagedMenu.register(services());
    ExampleControlsPaged.register(services());

    services().getService(TriggerListenerService.class).registerAll();

    services().getService(TypeBuiltinService.class).registerAll();

    LogicEngineService engine = services().getService(LogicEngineService.class);
    TriggerBusService bus = services().getService(TriggerBusService.class);
    TriggerRegistrationService registrations = services().getService(TriggerRegistrationService.class);
    
    if (engine instanceof DefaultLogicEngineService) {
      ((DefaultLogicEngineService) engine).setTriggerBus(bus);
    }
    
    if (registrations instanceof DefaultTriggerRegistrationService) {
      ((DefaultTriggerRegistrationService) registrations).setInternalOwner("nexlogic");
    }

    services().getService(ReloadService.class).reloadAsync();

    getLogger().info("NexLogic started.");
  }

  @Override
  protected void stop() {
    services().getService(RedisPubSubService.class).shutdown();
    services().getService(AsyncExecutorService.class).shutdown();
    getLogger().info("NexLogic is stopping...");
  }

  private void initDatabase(ServiceAccessor services) {
    HibernateMappingContributionService mapping = services.getService(HibernateMappingContributionService.class);
    mapping.contribute(this, EconomyBalanceEntity.class);
    mapping.contribute(this, BankMemberEntity.class);
    mapping.contribute(this, BankInviteEntity.class);
    mapping.contribute(this, BankWithdrawUsageEntity.class);
    mapping.contribute(this, BankTransactionEntity.class);
    mapping.contribute(this, BankAccountEntity.class);
    mapping.contribute(this, BankUnlockEntity.class);
    mapping.contribute(this, BankAccountLockEntity.class);

    DatabaseService db = services.getService(DatabaseService.class);
    db.start();

    DatabaseAsyncService dbAsync = services.getService(DatabaseAsyncService.class);
    dbAsync.start();

    RedisPubSubService pubSub = services.getService(RedisPubSubService.class);
    pubSub.start();
  }

}