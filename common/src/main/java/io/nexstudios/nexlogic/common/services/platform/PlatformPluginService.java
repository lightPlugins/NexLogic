package io.nexstudios.nexlogic.common.services.platform;

import io.nexstudios.serviceregistry.di.Service;

public interface PlatformPluginService extends Service {
  String name();
  String version();
}