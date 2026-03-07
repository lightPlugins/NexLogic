package io.nexstudios.nexlogic.common.types.triggers;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.serviceregistry.di.Service;

/**
 * One trigger = one class.
 * Responsible for registering/unregistering its Bukkit listeners.
 */
public interface TriggerSource extends Service {
  String id();

  void enable(PaperPluginService core);

  void disable();
}