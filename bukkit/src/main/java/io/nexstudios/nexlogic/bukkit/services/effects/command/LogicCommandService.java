package io.nexstudios.nexlogic.bukkit.services.effects.command;

import io.nexstudios.framework.paper.services.commands.annotations.Command;
import io.nexstudios.framework.paper.services.commands.annotations.CommandRoot;
import io.nexstudios.framework.paper.services.commands.source.NexPaperCommandSource;
import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.bukkit.services.effects.reload.ReloadService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.Service;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

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

  public LogicCommandService(PaperPluginService core) {
    this.services = core.plugin().services();
    this.reload = services.getService(ReloadService.class);
  }

  @Command(value = "reload", permission = "nexlogic.admin")
  public int reload(NexPaperCommandSource source) {
    source.sender().sendMessage("Reloading NexLogic...");
    reload.reloadAsync();
    source.sender().sendMessage("Reload scheduled.");
    return 1;
  }
}