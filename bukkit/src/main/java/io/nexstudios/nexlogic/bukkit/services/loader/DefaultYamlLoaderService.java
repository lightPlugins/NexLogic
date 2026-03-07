package io.nexstudios.nexlogic.bukkit.services.loader;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.bukkit.services.config.ConfigPathService;
import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

@Dependencies({
    ConfigPathService.class
})
public final class DefaultYamlLoaderService implements YamlLoaderService {

  private final ServiceAccessor services;
  private final ConfigPathService paths;
  private final Logger logger;
  private final Yaml yaml = new Yaml();

  public DefaultYamlLoaderService(PaperPluginService core) {
    this.services = core.plugin().services();
    this.paths = services.getService(ConfigPathService.class);
    this.logger = core.plugin().getLogger();
  }

  @Override
  public Map<String, Map<String, Object>> loadArguments() {
    Map<String, Map<String, Object>> out = new HashMap<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(paths.argumentsDir(), "*.yml")) {
      for (Path p : stream) {
        String id = stripExt(p.getFileName().toString()).toLowerCase();
        out.put(id, loadYamlMap(p));
      }
    } catch (Exception e) {
      logger.severe("Failed to read arguments folder: " + e.getMessage());
      e.printStackTrace();
    }
    return out;
  }

  /**
   * supported format:
   * actions/<file>.yml:
   *   enabled: true
   *   effects: [ ... ]
   */
  @Override
  public Map<String, List<ConfigSection>> loadEffectStylePacks(Map<String, Map<String, Object>> arguments) {
    Map<String, List<ConfigSection>> out = new HashMap<>();

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(paths.actionsDir(), "*.yml")) {
      for (Path p : stream) {
        String fileId = stripExt(p.getFileName().toString());
        String owner = "actions:" + fileId;

        Map<String, Object> root = loadYamlMap(p);
        SnakeYamlConfigSection cfg = new SnakeYamlConfigSection(root);

        boolean enabled = cfg.getBoolean("enabled", true);
        if (!enabled) continue;

        // only ROOT "effects" is allowed
        List<ConfigSection> effects = cfg.getSectionList("effects");
        if (effects.isEmpty()) continue;

        // keep custom args expansion (still works on effectEntry.args)
        List<ConfigSection> expanded = expandCustomArgs(effects, arguments, fileId, "effects");
        out.put(owner, expanded);
      }
    } catch (Exception e) {
      logger.severe("Failed to read actions folder for effects: " + e.getMessage());
      e.printStackTrace();
    }

    return out;
  }

  private List<ConfigSection> expandCustomArgs(
      List<ConfigSection> entries,
      Map<String, Map<String, Object>> arguments,
      String actionId,
      String sectionName
  ) {
    List<ConfigSection> out = new ArrayList<>();
    for (int i = 0; i < entries.size(); i++) {
      ConfigSection entry = entries.get(i);
      ConfigSection args = entry == null ? null : entry.getSection("args");
      if (!(args instanceof SnakeYamlConfigSection syArgs)) {
        if (entry != null) out.add(entry);
        continue;
      }

      Map<String, Object> mergedArgs = new LinkedHashMap<>(syArgs.getValues(true));
      for (String k : new ArrayList<>(mergedArgs.keySet())) {
        if (!k.startsWith("custom_")) continue;

        String argId = k.substring("custom_".length()).toLowerCase();
        Object caller = mergedArgs.remove(k);

        Map<String, Object> argRoot = arguments.get(argId);
        if (argRoot == null) {
          logger.severe("Unknown custom argument '" + argId + "' referenced at " + actionId + "." + sectionName + "[" + i + "]");
          continue;
        }

        Map<String, Object> tokens = caller instanceof Map<?, ?> m ? toStringObjectMap(m) : Map.of();
        Map<String, Object> expanded = deepCopy(argRoot);
        substituteTokens(expanded, tokens);

        deepMergeInto(mergedArgs, expanded);
      }

      Map<String, Object> entryMap = new LinkedHashMap<>(entry.getValues(true));
      entryMap.put("args", mergedArgs);
      out.add(new SnakeYamlConfigSection(entryMap));
    }
    return out;
  }

  private Map<String, Object> loadYamlMap(Path file) {
    try (InputStream in = Files.newInputStream(file)) {
      Object o = yaml.load(in);
      if (o == null) return new LinkedHashMap<>();
      if (!(o instanceof Map<?, ?> m)) throw new IllegalArgumentException("YAML root must be a map: " + file.getFileName());
      return toStringObjectMap(m);
    } catch (Exception e) {
      logger.severe("Failed to load YAML " + file.getFileName() + ": " + e.getMessage());
      e.printStackTrace();
      return new LinkedHashMap<>();
    }
  }

  private static String stripExt(String name) {
    int idx = name.lastIndexOf('.');
    return idx >= 0 ? name.substring(0, idx) : name;
  }

  private static Map<String, Object> toStringObjectMap(Map<?, ?> m) {
    Map<String, Object> out = new LinkedHashMap<>();
    for (var e : m.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
    return out;
  }

  private static Map<String, Object> deepCopy(Map<String, Object> in) {
    Map<String, Object> out = new LinkedHashMap<>();
    for (var e : in.entrySet()) {
      Object v = e.getValue();
      if (v instanceof Map<?, ?> m) out.put(e.getKey(), deepCopy(toStringObjectMap(m)));
      else if (v instanceof List<?> list) out.put(e.getKey(), new ArrayList<>(list));
      else out.put(e.getKey(), v);
    }
    return out;
  }

  private static void deepMergeInto(Map<String, Object> target, Map<String, Object> src) {
    for (var e : src.entrySet()) {
      Object v = e.getValue();
      if (v instanceof Map<?, ?> m && target.get(e.getKey()) instanceof Map<?, ?> tm) {
        Map<String, Object> tmm = toStringObjectMap(tm);
        deepMergeInto(tmm, toStringObjectMap(m));
        target.put(e.getKey(), tmm);
      } else {
        target.put(e.getKey(), v);
      }
    }
  }

  private static void substituteTokens(Map<String, Object> root, Map<String, Object> tokens) {
    for (var e : root.entrySet()) {
      Object v = e.getValue();
      if (v instanceof Map<?, ?> m) {
        Map<String, Object> child = toStringObjectMap(m);
        substituteTokens(child, tokens);
        e.setValue(child);
      } else if (v instanceof List<?> list) {
        List<Object> out = new ArrayList<>();
        for (Object o : list) {
          if (o instanceof Map<?, ?> mm) {
            Map<String, Object> child = toStringObjectMap(mm);
            substituteTokens(child, tokens);
            out.add(child);
          } else if (o instanceof String s) {
            out.add(replaceTokens(s, tokens));
          } else {
            out.add(o);
          }
        }
        e.setValue(out);
      } else if (v instanceof String s) {
        e.setValue(replaceTokens(s, tokens));
      }
    }
  }

  private static String replaceTokens(String s, Map<String, Object> tokens) {
    String out = s;
    for (var t : tokens.entrySet()) {
      out = out.replace("%" + t.getKey() + "%", String.valueOf(t.getValue()));
    }
    return out;
  }
}