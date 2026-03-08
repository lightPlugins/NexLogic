package io.nexstudios.nexlogic.common.services.engine;

import io.nexstudios.framework.core.key.NexKey;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.config.MapConfigSection;
import io.nexstudios.nexlogic.common.effects.model.CompiledAction;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.effects.runtime.ConditionInstance;
import io.nexstudios.nexlogic.common.effects.runtime.EffectInstance;
import io.nexstudios.nexlogic.common.services.filters.FilterService;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.nexlogic.common.services.platform.PlatformPluginService;
import io.nexstudios.nexlogic.common.services.registry.condition.ConditionTypeRegistryService;
import io.nexstudios.nexlogic.common.services.registry.effect.EffectTypeRegistryService;
import io.nexstudios.nexlogic.common.services.triggers.register.TriggerRegistrationService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.nexlogic.common.services.triggers.schema.TriggerContextSchemaService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.util.*;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

/**
 * A service for managing logic execution using registered conditions, effects, and triggers.
 */
@Dependencies({
    ConditionTypeRegistryService.class,
    EffectTypeRegistryService.class,
    FilterService.class,
    TriggerRegistrationService.class,
    TriggerContextSchemaService.class,
})
public final class DefaultLogicEngineService implements LogicEngineService {

  private final ConditionTypeRegistryService conditions;
  private final EffectTypeRegistryService effects;
  private final FilterService filters;
  private final TriggerRegistrationService registrations;
  private final TriggerContextSchemaService schema;
  private final LoggerService loggerService;
  private final String ownerNamespace;

  public DefaultLogicEngineService(ServiceAccessor accessor) {
    this.conditions = accessor.getService(ConditionTypeRegistryService.class);
    this.effects = accessor.getService(EffectTypeRegistryService.class);
    this.filters = accessor.getService(FilterService.class);
    this.registrations = accessor.getService(TriggerRegistrationService.class);
    this.schema = accessor.getService(TriggerContextSchemaService.class);
    this.loggerService = accessor.getService(LoggerService.class);
    PlatformPluginService platformPluginService = accessor.getService(PlatformPluginService.class);
    this.ownerNamespace = NexKey.normalizeNamespace(platformPluginService.name());
  }

  @Override
  public void registerEffectStyle(String owner, List<ConfigSection> effectEntries) {
    Objects.requireNonNull(owner, "owner");
    var compiled = compileEffectStyleToActions(owner, effectEntries);
    registrations.registerOwner(owner, compiled);
  }

  @Override
  public void registerTriggerStyle(String owner, List<ConfigSection> triggerEntries) {
    Objects.requireNonNull(owner, "owner");
    var compiled = compileTriggerStyleToActions(owner, triggerEntries);
    registrations.registerOwner(owner, compiled);
  }

  @Override
  public void unregisterOwner(String owner) {
    registrations.unregisterOwner(owner);
  }

  @Override
  public boolean testConditions(List<ConfigSection> conditionsList, LogicContext ctx) {
    Objects.requireNonNull(ctx, "ctx");
    try {
      var compiled = compileConditions(conditionsList);
      return safeTestAll(compiled, ctx);
    } catch (Throwable t) {
      loggerService.logger().severe("Invalid conditions list: " + t.getMessage());
      return false;
    }
  }

  @Override
  public void executeEffects(List<ConfigSection> effectsList, LogicContext ctx) {
    Objects.requireNonNull(ctx, "ctx");
    try {
      var compiled = compileEffects(effectsList);
      safeRunAll(compiled, ctx, "inline");
    } catch (Throwable t) {
      loggerService.logger().severe("Invalid effects list: " + t.getMessage());
    }
  }

  @Override
  public boolean fireEffectStyle(String triggerId, LogicContext ctx, List<ConfigSection> effectEntries) {
    Objects.requireNonNull(triggerId, "triggerId");
    Objects.requireNonNull(ctx, "ctx");
    if (effectEntries == null || effectEntries.isEmpty()) return false;

    boolean firedAny = false;
    String t = triggerId.toLowerCase();

    for (ConfigSection entry : effectEntries) {
      if (entry == null) continue;

      if (!containsTrigger(entry.getSectionList("triggers"), t)) continue;

      if (!safeTestAll(compileConditions(entry.getSectionList("conditions")), ctx)) continue;

      Predicate<LogicContext> fp;
      try {
        fp = filters.compile(t, entry.getSection("filters"));
      } catch (Throwable ex) {
        String effectId = entry.getString("id", "?");
        String filterId = extractFilterId(ex.getMessage()).orElse("?");
        loggerService.logger().severe("Filter '" + filterId + "' is not compatible with trigger '" + t + "' for effect '" + effectId + "'.");
        return false;
      }

      boolean filtersOk;
      try {
        filtersOk = fp.test(ctx);
      } catch (Throwable ex) {
        loggerService.logger().severe("A filter threw an exception while evaluating trigger '" + t + "': " + ex.getMessage());
        continue;
      }
      if (!filtersOk) continue;

      String effectId = entry.getString("id", null);
      if (effectId == null) continue;

      EffectInstance inst;
      try {
        var svc = effects.resolve(effectId).orElseThrow(() ->
            new IllegalArgumentException("Unknown effect id '" + effectId + "'")
        );
        ConfigSection args = entry.getSection("args");
        inst = svc.create(args == null ? MapConfigSection.EMPTY : args);
      } catch (Throwable ex) {
        loggerService.logger().severe("Unknown/invalid effect '" + effectId + "' (trigger '" + t + "'): " + ex.getMessage());
        continue;
      }

      safeRunAll(List.of(inst), ctx, effectId);
      firedAny = true;
    }

    return firedAny;
  }

  @Override
  public boolean fireTriggerStyle(String triggerId, LogicContext ctx, List<ConfigSection> triggerEntries) {
    Objects.requireNonNull(triggerId, "triggerId");
    Objects.requireNonNull(ctx, "ctx");
    if (triggerEntries == null || triggerEntries.isEmpty()) return false;

    boolean firedAny = false;
    String t = triggerId.toLowerCase();

    for (ConfigSection triggerEntry : triggerEntries) {
      if (triggerEntry == null) continue;

      String id = triggerEntry.getString("id", null);
      if (id == null || !id.equalsIgnoreCase(t)) continue;

      if (!safeTestAll(compileConditions(triggerEntry.getSectionList("conditions")), ctx)) continue;

      Predicate<LogicContext> fp;
      try {
        fp = filters.compile(t, triggerEntry.getSection("filters"));
      } catch (Throwable ex) {
        String filterId = extractFilterId(ex.getMessage()).orElse("?");
        loggerService.logger().severe("Filter '" + filterId + "' is not compatible with trigger '" + t + "'.");
        return false;
      }

      boolean filtersOk;
      try {
        filtersOk = fp.test(ctx);
      } catch (Throwable ex) {
        loggerService.logger().severe("A filter threw an exception while evaluating trigger '" + t + "': " + ex.getMessage());
        continue;
      }
      if (!filtersOk) continue;

      var compiledEffects = compileEffects(triggerEntry.getSectionList("effects"));
      safeRunAll(compiledEffects, ctx, "trigger:" + t);
      firedAny = true;
    }

    return firedAny;
  }

  private @NotNull String actionIdForEffectBinding(String owner, String effectId, String triggerIdLower) {
    String safeOwner = sanitizeKeyPath(owner);
    String safeEffect = sanitizeKeySegment(effectId);
    String safeTrigger = sanitizeKeySegment(triggerIdLower);

    // key must match: [a-z0-9/._-]+
    String key = safeOwner + "/effect/" + safeEffect + "/trigger/" + safeTrigger;
    return NexKey.of(ownerNamespace, key).toString();
  }

  private @NotNull String actionIdForTriggerBinding(String owner, String triggerIdLower) {
    String safeOwner = sanitizeKeyPath(owner);
    String safeTrigger = sanitizeKeySegment(triggerIdLower);

    String key = safeOwner + "/trigger/" + safeTrigger;
    return NexKey.of(ownerNamespace, key).toString();
  }

  private static @NotNull String sanitizeKeyPath(String in) {
    if (in == null || in.isBlank()) return "unknown";
    String s = NexKey.normalizeKey(in);

    // allow path separators, replace ":" and other invalid chars
    s = s.replace(':', '/');

    // keep only allowed chars: [a-z0-9/._-]
    s = s.replaceAll("[^a-z0-9/._-]", "_");

    // avoid accidental empty segments like "actions://"
    s = s.replaceAll("/{2,}", "/");
    if (s.startsWith("/")) s = s.substring(1);
    if (s.endsWith("/")) s = s.substring(0, s.length() - 1);

    return s.isBlank() ? "unknown" : s;
  }

  private static @NotNull String sanitizeKeySegment(String in) {
    if (in == null || in.isBlank()) return "unknown";
    String s = NexKey.normalizeKey(in);

    // segments must not contain "/" ideally; convert it to "_"
    s = s.replace('/', '_');

    // keep only allowed chars: [a-z0-9._-]
    s = s.replaceAll("[^a-z0-9._-]", "_");

    return s.isBlank() ? "unknown" : s;
  }

  private Map<String, List<CompiledAction>> compileEffectStyleToActions(String owner, List<ConfigSection> effectEntries) {
    if (effectEntries == null || effectEntries.isEmpty()) return Map.of();

    Map<String, List<CompiledAction>> out = new HashMap<>();

    for (int entryIndex = 0; entryIndex < effectEntries.size(); entryIndex++) {
      ConfigSection entry = effectEntries.get(entryIndex);
      if (entry == null) continue;

      String effectId = entry.getString("id", null);
      if (effectId == null || effectId.isBlank()) {
        loggerService.logger().severe("Invalid effect entry (missing id). Found in file: " + owner + ".yml");
        continue;
      }

      ConfigSection effectArgs = entry.getSection("args");

      List<ConfigSection> triggerBindings = entry.getSectionList("trigger");
      if (triggerBindings == null || triggerBindings.isEmpty()) {
        loggerService.logger().severe("Effect '" + effectId + "' has no trigger bindings. Found in file: " + owner + ".yml");
        continue;
      }

      for (ConfigSection binding : triggerBindings) {
        if (binding == null) continue;

        String triggerId = binding.getString("id", null);
        if (triggerId == null || triggerId.isBlank()) {
          loggerService.logger().severe(
              "Effect '" + effectId + "' has a trigger binding without id. Found in file: " + owner + ".yml"
          );
          continue;
        }
        String triggerIdLower = triggerId.toLowerCase();

        Set<ContextCapability> caps = schema.capabilities(triggerIdLower);

        List<ConditionInstance> entryConditions;
        try {
          entryConditions = compileConditionsValidated(
              caps,
              binding.getSectionList("conditions"),
              owner,
              effectId,
              triggerIdLower,
              entryIndex
          );
        } catch (Throwable ex) {
          loggerService.logger().severe(
              "Invalid conditions for effect '" + effectId + "' on trigger '" + triggerIdLower + "'. Found in file: " + owner + ".yml"
          );
          continue;
        }

        EffectInstance baseEffect;
        try {
          baseEffect = compileSingleEffectValidated(caps, effectId, effectArgs, owner, triggerIdLower, entryIndex);
        } catch (Throwable ex) {
          continue;
        }

        try {
          ConfigSection normalizedFilters = normalizeFilterList(binding.getSectionList("filters"));
          Predicate<LogicContext> fp = filters.compile(triggerIdLower, normalizedFilters);

          List<ConditionInstance> allConds = new ArrayList<>(entryConditions);
          allConds.add(fp::test);

          CompiledAction ca = new CompiledAction(
              actionIdForEffectBinding(owner, effectId, triggerIdLower),
              List.of(triggerIdLower),
              List.copyOf(allConds),
              List.of(baseEffect)
          );

          out.computeIfAbsent(triggerIdLower, k -> new ArrayList<>()).add(ca);
        } catch (Throwable ex) {
          String filterId = extractFilterId(ex.getMessage()).orElse("?");
          loggerService.logger().severe(
              "Filter '" + filterId + "' is not compatible with trigger '" + triggerIdLower +
                  "' for effect '" + effectId + "'. Found in file: " + owner + ".yml"
          );
        }
      }
    }

    Map<String, List<CompiledAction>> frozen = new HashMap<>();
    for (var e : out.entrySet()) frozen.put(e.getKey(), List.copyOf(e.getValue()));
    return Map.copyOf(frozen);
  }

  private Map<String, List<CompiledAction>> compileTriggerStyleToActions(String owner, List<ConfigSection> triggerEntries) {
    if (triggerEntries == null || triggerEntries.isEmpty()) return Map.of();

    Map<String, List<CompiledAction>> out = new HashMap<>();

    for (int triggerIndex = 0; triggerIndex < triggerEntries.size(); triggerIndex++) {
      ConfigSection tEntry = triggerEntries.get(triggerIndex);
      if (tEntry == null) continue;

      String triggerId = tEntry.getString("id", null);
      if (triggerId == null || triggerId.isBlank()) {
        loggerService.logger().severe("Invalid trigger entry (missing id). Found in file: " + owner);
        continue;
      }
      String triggerIdLower = triggerId.toLowerCase();

      Set<ContextCapability> caps = schema.capabilities(triggerIdLower);

      List<ConditionInstance> triggerConditions;
      try {
        triggerConditions = compileConditionsValidated(caps, tEntry.getSectionList("conditions"), owner, "trigger:" + triggerIdLower, triggerIdLower, triggerIndex);
      } catch (Throwable ex) {
        loggerService.logger().severe("Invalid conditions for trigger '" + triggerIdLower + "'. Found in file: " + owner + ".yml");
        continue;
      }

      Predicate<LogicContext> fp;
      try {
        fp = filters.compile(triggerIdLower, tEntry.getSection("filters"));
      } catch (Throwable ex) {
        String filterId = extractFilterId(ex.getMessage()).orElse("?");
        loggerService.logger().severe("Filter '" + filterId + "' is not compatible with trigger '" + triggerIdLower + "'. Found in file: " + owner + ".yml");
        continue;
      }

      List<ConditionInstance> allConds = new ArrayList<>(triggerConditions);
      allConds.add(fp::test);

      List<EffectInstance> compiledEffects = new ArrayList<>();
      var effectsList = tEntry.getSectionList("effects");

      for (int effectIndex = 0; effectIndex < effectsList.size(); effectIndex++) {
        ConfigSection eEntry = effectsList.get(effectIndex);
        if (eEntry == null) continue;

        String effectId = eEntry.getString("id", null);
        if (effectId == null || effectId.isBlank()) {
          loggerService.logger().severe("Invalid nested effect (missing id). Found in file: " + owner + ".yml");
          continue;
        }

        try {
          compiledEffects.add(
              compileSingleEffectValidated(caps, effectId, eEntry.getSection("args"), owner, triggerIdLower, effectIndex)
          );
        } catch (Throwable ignored) {
          // compileSingleEffectValidated already logged; skip this nested effect
        }
      }

      if (compiledEffects.isEmpty()) {
        loggerService.logger().severe("Trigger '" + triggerIdLower + "' has no valid effects after compatibility checks. Found in file: " + owner + ".yml");
        continue;
      }

      CompiledAction ca = new CompiledAction(
          actionIdForTriggerBinding(owner, triggerIdLower),
          List.of(triggerIdLower),
          List.copyOf(allConds),
          List.copyOf(compiledEffects)
      );

      out.computeIfAbsent(triggerIdLower, k -> new ArrayList<>()).add(ca);
    }

    Map<String, List<CompiledAction>> frozen = new HashMap<>();
    for (var e : out.entrySet()) frozen.put(e.getKey(), List.copyOf(e.getValue()));
    return Map.copyOf(frozen);
  }

  private static ConfigSection normalizeFilterList(List<ConfigSection> filtersList) {
    if (filtersList == null || filtersList.isEmpty()) return null;

    Map<String, Object> out = new LinkedHashMap<>();

    for (ConfigSection f : filtersList) {
      if (f == null) continue;

      String id = f.getString("id", null);
      if (id == null || id.isBlank()) {
        throw new IllegalArgumentException("Filter entry missing 'id'");
      }

      Map<String, Object> raw = new LinkedHashMap<>(f.getValues(true));
      raw.remove("id");

      Object args = raw.get("args");
      if (!(args instanceof Map<?, ?>)) {
        args = raw; // treat remaining keys as args directly
      }

      out.put(id, Map.of("args", args));
    }

    return new MapConfigSection(out);
  }

  private List<ConditionInstance> compileConditionsValidated(
      Set<ContextCapability> caps,
      List<ConfigSection> conditionEntries,
      String owner,
      String subjectId,
      String triggerIdLower,
      int indexForLog
  ) {
    if (conditionEntries == null || conditionEntries.isEmpty()) return List.of();

    List<ConditionInstance> out = new ArrayList<>();
    for (int i = 0; i < conditionEntries.size(); i++) {
      ConfigSection entry = conditionEntries.get(i);
      if (entry == null) continue;

      String id = entry.getString("id", null);
      if (id == null) throw new IllegalArgumentException("Condition entry missing 'id'");

      var svc = conditions.resolve(id).orElseThrow(() ->
          new IllegalArgumentException("Unknown condition id '" + id + "'")
      );

      Set<ContextCapability> required = svc.requiredCapabilities();
      if (!caps.containsAll(required)) {
        Set<ContextCapability> missing = EnumSet.copyOf(required);
        missing.removeAll(caps);

        loggerService.logger().severe(
            "Skipping condition '" + id + "' for '" + subjectId + "' on trigger '" + triggerIdLower + "' (owner '" + owner + "'): " +
                "missing capabilities " + missing + ", provided " + caps + " (entryIndex=" + indexForLog + ", conditionIndex=" + i + ")"
        );
        throw new IllegalArgumentException("Incompatible condition '" + id + "'");
      }

      ConfigSection args = entry.getSection("args");
      out.add(svc.create(args == null ? MapConfigSection.EMPTY : args));
    }

    return List.copyOf(out);
  }

  private EffectInstance compileSingleEffectValidated(
      Set<ContextCapability> caps,
      String effectId,
      ConfigSection args,
      String owner,
      String triggerIdLower,
      int indexForLog
  ) {
    var svc = effects.resolve(effectId).orElseThrow(() ->
        new IllegalArgumentException("Unknown effect id '" + effectId + "' at " + owner)
    );

    Set<ContextCapability> required = svc.requiredCapabilities();
    if (!caps.containsAll(required)) {
      Set<ContextCapability> missing = EnumSet.copyOf(required);
      missing.removeAll(caps);

      loggerService.logger().severe(
          "Skipping effect '" + effectId + "' on trigger '" + triggerIdLower + "' (owner '" + owner + "'): " +
              "missing capabilities " + missing + ", provided " + caps + " (index=" + indexForLog + ")"
      );
      throw new IllegalArgumentException("Incompatible effect '" + effectId + "'");
    }

    return svc.create(args == null ? MapConfigSection.EMPTY : args);
  }

  private List<ConditionInstance> compileConditions(List<ConfigSection> conditionEntries) {
    if (conditionEntries == null || conditionEntries.isEmpty()) return List.of();

    List<ConditionInstance> out = new ArrayList<>();
    for (ConfigSection entry : conditionEntries) {
      if (entry == null) continue;

      String id = entry.getString("id", null);
      if (id == null) throw new IllegalArgumentException("Condition entry missing 'id'");

      var svc = conditions.resolve(id).orElseThrow(() ->
          new IllegalArgumentException("Unknown condition id '" + id + "'")
      );

      ConfigSection args = entry.getSection("args");
      out.add(svc.create(args == null ? MapConfigSection.EMPTY : args));
    }
    return List.copyOf(out);
  }

  private @NotNull List<EffectInstance> compileEffects(List<ConfigSection> effectEntries) {
    if (effectEntries == null || effectEntries.isEmpty()) return List.of();

    List<EffectInstance> out = new ArrayList<>();
    for (ConfigSection entry : effectEntries) {
      if (entry == null) continue;

      String id = entry.getString("id", null);
      if (id == null) throw new IllegalArgumentException("Effect entry missing 'id'");

      var svc = effects.resolve(id).orElseThrow(() ->
          new IllegalArgumentException("Unknown effect id '" + id + "'")
      );

      ConfigSection args = entry.getSection("args");
      out.add(svc.create(args == null ? MapConfigSection.EMPTY : args));
    }
    return out;
  }

  private EffectInstance compileSingleEffect(String id, ConfigSection args, String where) {
    var svc = effects.resolve(id).orElseThrow(() ->
        new IllegalArgumentException("Unknown effect id '" + id + "' at " + where)
    );
    return svc.create(args == null ? MapConfigSection.EMPTY : args);
  }

  private boolean safeTestAll(@NotNull List<ConditionInstance> compiled, LogicContext ctx) {
    for (var c : compiled) {
      boolean ok;
      try {
        ok = c.test(ctx);
      } catch (Throwable t) {
        loggerService.logger().severe("A condition threw an exception: " + t.getMessage());
        return false;
      }
      if (!ok) return false;
    }
    return true;
  }

  private void safeRunAll(@NotNull List<EffectInstance> compiled, LogicContext ctx, String tag) {
    for (var e : compiled) {
      try {
        e.run(ctx);
      } catch (Throwable t) {
        loggerService.logger().severe("An effect threw an exception (" + tag + "): " + t.getMessage());
      }
    }
  }

  private static boolean containsTrigger(List<ConfigSection> list, String triggerIdLower) {
    if (list == null || list.isEmpty()) return false;
    for (ConfigSection s : list) {
      if (s == null) continue;
      String v = s.getString("value", null);
      if (v != null && v.equalsIgnoreCase(triggerIdLower)) return true;
    }
    return false;
  }

  private static List<String> toLowerStringList(List<ConfigSection> list) {
    if (list == null || list.isEmpty()) return List.of();
    List<String> out = new ArrayList<>();
    for (ConfigSection section : list) {
      if (section == null) continue;
      String v = section.getString("value", null);
      if (v != null && !v.isBlank()) out.add(v.toLowerCase());
    }
    return List.copyOf(out);
  }

  /**
   * Extracts and returns an identifier located between single quotes within the given message string.
   * If the identifier cannot be found or the message is null, an empty {@code Optional} is returned.
   *
   * @param msg the input message string containing the identifier in single quotes
   * @return an {@code Optional} containing the identifier if found and non-blank, or an empty {@code Optional} if not found
   */
  private static Optional<String> extractFilterId(String msg) {
    if (msg == null) return Optional.empty();
    int a = msg.indexOf('\'');
    if (a < 0) return Optional.empty();
    int b = msg.indexOf('\'', a + 1);
    if (b < 0) return Optional.empty();
    String s = msg.substring(a + 1, b).trim();
    return s.isBlank() ? Optional.empty() : Optional.of(s);
  }
}