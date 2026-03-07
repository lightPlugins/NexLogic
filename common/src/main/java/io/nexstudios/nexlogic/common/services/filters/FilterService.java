package io.nexstudios.nexlogic.common.services.filters;

import io.nexstudios.nexlogic.common.config.ConfigSection;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.serviceregistry.di.Service;

import java.util.function.Predicate;

public interface FilterService extends Service {

  /**
   * Compiles a "filters:" section to a predicate for a given trigger.
   *
   * Example for break_block:
   * filters:
   *   blocks: [stone, netherrack]
   */
  Predicate<LogicContext> compile(String triggerId, ConfigSection filters);
}