package io.nexstudios.nexlogic.common.services.registry.addon;

import io.nexstudios.nexlogic.common.addon.NexLogicAddon;
import io.nexstudios.serviceregistry.di.Service;

public interface AddonRegistryService extends Service {
  void registerAddon(NexLogicAddon addon);
}