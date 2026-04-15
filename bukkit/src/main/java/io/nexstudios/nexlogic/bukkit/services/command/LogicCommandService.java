package io.nexstudios.nexlogic.bukkit.services.command;

import io.nexstudios.commandservice.service.commands.annotations.Command;
import io.nexstudios.commandservice.service.commands.annotations.CommandRoot;
import io.nexstudios.commandservice.service.commands.source.NexPaperCommandSource;
import io.nexstudios.menuservice.api.MenuRegistry;
import io.nexstudios.menuservice.api.MenuService;
import io.nexstudios.menuservice.demo.definition.DemoMenuDefinition;
import io.nexstudios.nexlogic.bukkit.services.effects.reload.ReloadService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.Service;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.entity.Player;

@CommandRoot(
    name = "nexlogic",
    description = "NexLogic admin command"
)
@Dependencies({
    ReloadService.class
})
public final class LogicCommandService implements Service {

  private final ServiceAccessor services;
  private final ReloadService reload;

  public LogicCommandService(ServiceAccessor accessor) {
    this.reload = accessor.getService(ReloadService.class);
    this.services = accessor;
  }

  @Command(value = "reload", permission = "nexlogic.admin")
  public int reload(NexPaperCommandSource source) {
    source.sender().sendMessage("Reloading NexLogic...");
    reload.reloadAsync();
    source.sender().sendMessage("Reload scheduled.");
    return 1;
  }

  @Command(value = "open", permission = "nexlogic.admin")
  public int openDemoMenu(NexPaperCommandSource source) {

    Player player = (Player) source.sender();
    if(player == null) {
      source.sender().sendMessage("This command can only be executed by an player!");
    }

    services.getService(MenuService.class).open(player, DemoMenuDefinition.KEY);

    return 1;
  }
}