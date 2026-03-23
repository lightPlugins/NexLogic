package io.nexstudios.nexlogic.bukkit.services.hooks.towny;

import com.palmergames.bukkit.towny.TownyEconomyHandler;

import java.util.Optional;
import java.util.UUID;

public class DefaultTownyService implements TownyService {

  @Override

  public Optional<UUID> getTownUUID(String townName) {
    return Optional.ofNullable(TownyEconomyHandler.getTownyObjectUUID(townName));
  }
}
