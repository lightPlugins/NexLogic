package io.nexstudios.nexlogic.bukkit.services.hooks.nexo;

import com.nexomc.nexo.api.NexoItems;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

public class DefaultNexoService implements NexoService {

  private final NexoItems nexoItems;

  public DefaultNexoService(ServiceAccessor accessor) {
    this.nexoItems = NexoItems.INSTANCE;
  }


}
