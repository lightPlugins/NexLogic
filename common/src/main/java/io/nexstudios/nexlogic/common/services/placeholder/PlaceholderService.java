package io.nexstudios.nexlogic.common.services.placeholder;

import io.nexstudios.nexlogic.common.placeholder.PlaceholderKey;
import io.nexstudios.nexlogic.common.placeholder.PlaceholderProvider;
import io.nexstudios.nexlogic.common.placeholder.PlaceholderResolveContext;
import io.nexstudios.serviceregistry.di.Service;

import java.time.Duration;
import java.util.Optional;

public interface PlaceholderService extends Service {

  void register(String owner, PlaceholderKey key, PlaceholderProvider provider, Duration ttl);

  default void register(String owner, String identifier, String id, PlaceholderProvider provider, Duration ttl) {
    register(owner, PlaceholderKey.of(identifier, id), provider, ttl);
  }

  void unregisterOwner(String owner);

  Optional<String> resolveSingle(String token, PlaceholderResolveContext ctx, ResolveOptions options);

  String resolveText(String input, PlaceholderResolveContext ctx, ResolveOptions options);

  record ResolveOptions(
      int maxDepth,
      int maxTokensPerInput
  ) {
    public ResolveOptions {
      if (maxDepth < 1) throw new IllegalArgumentException("maxDepth must be >= 1");
      if (maxTokensPerInput < 1) throw new IllegalArgumentException("maxTokensPerInput must be >= 1");
    }

    public static ResolveOptions defaults() {
      return new ResolveOptions(16, 4096);
    }
  }
}