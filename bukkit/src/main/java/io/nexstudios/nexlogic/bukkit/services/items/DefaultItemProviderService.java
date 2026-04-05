package io.nexstudios.nexlogic.bukkit.services.items;

import io.nexstudios.nexlogic.bukkit.services.items.providers.VanillaItemProvider;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Dependencies({
    LoggerService.class
})
public final class DefaultItemProviderService implements ItemProviderService {

  private final LoggerService logger;
  private final List<Provider> providers = new CopyOnWriteArrayList<>();

  public DefaultItemProviderService(ServiceAccessor services) {
    this.logger = services.getService(LoggerService.class);
    this.providers.add(new VanillaItemProvider());
  }

  @Override
  public void registerProvider(Provider provider) {
    providers.add(Objects.requireNonNull(provider, "provider"));
  }

  @Override
  public Optional<ItemStack> getItem(String namespace) {
    String normalized = normalize(namespace);
    if (normalized.isEmpty()) {
      return Optional.empty();
    }

    List<String> attemptedProviders = new ArrayList<>(providers.size());

    for (Provider provider : providers) {
      if (!provider.supports(normalized)) {
        continue;
      }

      attemptedProviders.add(provider.providerName());
      Optional<ItemStack> item = provider.getItem(normalized);
      if (item.isPresent()) {
        return item;
      }
    }

    if (attemptedProviders.isEmpty()) {
      logger.logger().warning("The namespace '" + normalized + "' was not found in any registered item provider because no provider accepted it.");
      return Optional.empty();
    }

    for (String providerName : attemptedProviders) {
      logger.logger().warning("The namespace '" + normalized + "' was not found in the " + providerName + " item provider.");
    }

    return Optional.empty();
  }

  private static String normalize(String namespace) {
    if (namespace == null) {
      return "";
    }

    String normalized = namespace.trim().toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()) {
      return normalized;
    }

    if (!normalized.contains(":")) {
      return "minecraft:" + normalized;
    }

    return normalized;
  }
}


