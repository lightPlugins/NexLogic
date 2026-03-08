package io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.options.resolve;

import io.nexstudios.nexlogic.common.services.placeholder.PlaceholderService;
import io.nexstudios.serviceregistry.di.Service;

public interface PlaceholderResolveOptionsService extends Service {

  PlaceholderService.ResolveOptions options();
  void reload();
}