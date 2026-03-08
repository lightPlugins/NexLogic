package io.nexstudios.nexlogic.bukkit.services.placeholder.runtime;

import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.serviceregistry.di.Service;

public interface PlaceholderRuntimeService extends Service {

  String resolve(String input, LogicContext ctx);
  ConfigSection resolveSection(ConfigSection input, LogicContext ctx);
}