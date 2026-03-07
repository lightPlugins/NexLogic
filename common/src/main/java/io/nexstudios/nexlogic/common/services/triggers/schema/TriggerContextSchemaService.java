package io.nexstudios.nexlogic.common.services.triggers.schema;

import io.nexstudios.serviceregistry.di.Service;

import java.util.Set;

public interface TriggerContextSchemaService extends Service {
  Set<ContextCapability> capabilities(String triggerId);
}