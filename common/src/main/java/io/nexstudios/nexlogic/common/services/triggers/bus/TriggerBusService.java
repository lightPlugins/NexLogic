package io.nexstudios.nexlogic.common.services.triggers.bus;

import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.serviceregistry.di.Service;

public interface TriggerBusService extends Service {
  void fire(String triggerId, LogicContext ctx);
}