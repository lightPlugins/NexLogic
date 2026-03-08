package io.nexstudios.nexlogic.bukkit.services.placeholder.runtime;

import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextKeys;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.options.resolve.PlaceholderResolveOptionsService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.config.MapConfigSection;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.placeholder.PlaceholderResolveContext;
import io.nexstudios.nexlogic.common.services.placeholder.PlaceholderService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.entity.Player;

import java.util.*;

@Dependencies({
    PlaceholderService.class,
    PlaceholderResolveOptionsService.class
})
public final class DefaultPlaceholderRuntimeService implements PlaceholderRuntimeService {

  private static final String ID_KEY = "id";

  private final PlaceholderService placeholders;
  private final PlaceholderResolveOptionsService optionsService;

  public DefaultPlaceholderRuntimeService(ServiceAccessor services) {
    this.placeholders = services.getService(PlaceholderService.class);
    this.optionsService = services.getService(PlaceholderResolveOptionsService.class);
  }

  @Override
  public String resolve(String input, LogicContext ctx) {
    if (input == null || input.isEmpty()) return input == null ? "" : input;

    PlaceholderResolveContext prc = new PlaceholderResolveContext(
        ctx,
        "",
        scopeKey(ctx),
        Map.of()
    );

    return placeholders.resolveText(input, prc, optionsService.options());
  }

  @Override
  public ConfigSection resolveSection(ConfigSection input, LogicContext ctx) {
    if (input == null) return MapConfigSection.EMPTY;

    PlaceholderResolveContext prc = new PlaceholderResolveContext(
        ctx,
        "",
        scopeKey(ctx),
        Map.of()
    );

    Map<String, Object> resolved = resolveObjectMap(input.getValues(true), prc);
    return new MapConfigSection(resolved);
  }

  private Map<String, Object> resolveObjectMap(Map<String, Object> in, PlaceholderResolveContext prc) {
    if (in == null || in.isEmpty()) return Map.of();

    Map<String, Object> out = new LinkedHashMap<>(in.size());

    for (var e : in.entrySet()) {
      String key = e.getKey();
      Object value = e.getValue();

      if (key != null && key.equalsIgnoreCase(ID_KEY) && value instanceof String) {
        out.put(key, value);
        continue;
      }

      out.put(key, resolveAny(value, prc));
    }

    return out;
  }

  private Object resolveAny(Object value, PlaceholderResolveContext prc) {
    switch (value) {
      case null -> {
        return null;
      }
      case String s -> {
        return placeholders.resolveText(s, prc, optionsService.options());
      }
      case Map<?, ?> m -> {
        Map<String, Object> mm = toStringObjectMap(m);

        Map<String, Object> out = new LinkedHashMap<>(mm.size());
        for (var e : mm.entrySet()) {
          String key = e.getKey();
          Object v = e.getValue();

          if (key != null && key.equalsIgnoreCase(ID_KEY) && v instanceof String) {
            out.put(key, v);
            continue;
          }

          out.put(key, resolveAny(v, prc));
        }
        return out;
      }
      case List<?> list -> {
        List<Object> out = new ArrayList<>(list.size());
        for (Object o : list) out.add(resolveAny(o, prc));
        return out;
      }
      default -> {
      }
    }

    return value;
  }

  private static Map<String, Object> toStringObjectMap(Map<?, ?> m) {
    Map<String, Object> out = new LinkedHashMap<>();
    for (var e : m.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
    return out;
  }

  private static String scopeKey(LogicContext ctx) {
    if (ctx == null) return "global";

    UUID uuid = ctx.get(BukkitContextKeys.PLAYER)
        .map(Player::getUniqueId)
        .orElse(null);

    return uuid == null ? "global" : ("player:" + uuid);
  }
}