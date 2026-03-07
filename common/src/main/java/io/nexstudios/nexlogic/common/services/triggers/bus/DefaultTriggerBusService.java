package io.nexstudios.nexlogic.common.services.triggers.bus;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.common.model.CompiledAction;
import io.nexstudios.nexlogic.common.model.LogicContext;
import io.nexstudios.nexlogic.common.services.triggers.register.TriggerRegistrationService;
import io.nexstudios.nexlogic.common.services.triggers.runtime.TriggerRuntimeService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Dependencies({
    TriggerRuntimeService.class,
    TriggerRegistrationService.class
})
public final class DefaultTriggerBusService implements TriggerBusService {

  private final ServiceAccessor services;
  private final AtomicReference<Map<String, List<CompiledAction>>> active = new AtomicReference<>(Map.of());
  private final TriggerRuntimeService runtime;
  private final TriggerRegistrationService registrations;

  public DefaultTriggerBusService(PaperPluginService core) {
    this.services = core.plugin().services();
    this.runtime = services.getService(TriggerRuntimeService.class);
    this.registrations = services.getService(TriggerRegistrationService.class);
  }

  @Override
  public void fire(String triggerId, LogicContext ctx) {
    var map = active.get();
    String t = triggerId == null ? "" : triggerId.toLowerCase();

    var base = map.getOrDefault(t, Collections.emptyList());
    var combined = registrations.combine(t, base);

    if (combined.isEmpty()) return;
    runtime.executeAll(combined, ctx);
  }
}