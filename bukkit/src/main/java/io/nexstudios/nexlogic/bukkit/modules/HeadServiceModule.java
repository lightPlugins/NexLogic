package io.nexstudios.nexlogic.bukkit.modules;

import io.nexstudios.nexlogic.bukkit.services.heads.DefaultHeadService;
import io.nexstudios.nexlogic.bukkit.services.heads.HeadService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;

public final class HeadServiceModule implements ServiceModule {

  @Override
  public void install(ServiceAccessor serviceAccessor) {
    serviceAccessor.register(HeadService.class, DefaultHeadService.class);
  }
}
