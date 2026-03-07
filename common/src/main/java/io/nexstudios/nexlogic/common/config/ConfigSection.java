package io.nexstudios.nexlogic.common.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ConfigSection {
  String getString(String path, String def);
  int getInt(String path, int def);
  boolean getBoolean(String path, boolean def);
  double getDouble(String path, double def);

  ConfigSection getSection(String path);
  List<ConfigSection> getSectionList(String path);

  Set<String> getKeys(boolean deep);
  Map<String, Object> getValues(boolean deep);
}