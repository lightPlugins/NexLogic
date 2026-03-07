package io.nexstudios.nexlogic.bukkit.services.loader;

import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.serviceregistry.di.Service;

import java.util.List;
import java.util.Map;

public interface YamlLoaderService extends Service {
  Map<String, Map<String, Object>> loadArguments();

  /**
   * Reads Style-1 effect entries from "test.effects" in each actions/*.yml file.
   *
   * Return: ownerId -> list of effect-style entries
   */
  Map<String, List<ConfigSection>> loadEffectStylePacks(Map<String, Map<String, Object>> arguments);
}