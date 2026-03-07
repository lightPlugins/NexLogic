package io.nexstudios.nexlogic.common.services.logging;

import io.nexstudios.serviceregistry.di.Service;

import java.util.logging.Logger;

public interface LoggerService extends Service {
  Logger logger();
}