package io.nexstudios.nexlogic.bukkit.modules;

import io.nexstudios.nexlogic.bukkit.services.expression.DefaultExpressionService;
import io.nexstudios.nexlogic.bukkit.services.expression.ExpressionService;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import io.nexstudios.serviceregistry.di.ServiceModule;

public class UtilityServiceModule implements ServiceModule {

  @Override
  public void install(ServiceAccessor serviceAccessor) {

    serviceAccessor.register(ExpressionService.class, DefaultExpressionService.class);
  }
}
