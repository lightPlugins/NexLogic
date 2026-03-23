package io.nexstudios.nexlogic.bukkit.services.hooks.towny;

import io.nexstudios.serviceregistry.di.Service;

import java.util.Optional;
import java.util.UUID;

public interface TownyService extends Service {


  Optional<UUID> getTownUUID(String townName);

}
