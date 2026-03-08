package io.nexstudios.nexlogic.bukkit.services.placeholder;

import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.options.resolve.PlaceholderResolveOptionsService;
import io.nexstudios.nexlogic.common.placeholder.PlaceholderResolveContext;
import io.nexstudios.nexlogic.common.placeholder.loader.PlaceholderFileModel;
import io.nexstudios.nexlogic.common.placeholder.providers.EvalExExpressionPlaceholderProvider;
import io.nexstudios.nexlogic.common.services.placeholder.PlaceholderService;
import io.nexstudios.nexlogic.common.services.placeholder.loader.PlaceholderYamlLoaderService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.framework.paper.services.plugin.PaperPluginService;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

@Dependencies({
    PlaceholderService.class,
    PlaceholderYamlLoaderService.class,
    PlaceholderResolveOptionsService.class
})
public final class DefaultPlaceholderReloadService implements PlaceholderReloadService {

  private final PlaceholderService placeholders;
  private final PlaceholderYamlLoaderService loader;
  private final PlaceholderResolveOptionsService resolveOptions;
  private final Logger logger;

  public DefaultPlaceholderReloadService(PaperPluginService core) {
    this.placeholders = core.plugin().services().getService(PlaceholderService.class);
    this.loader = core.plugin().services().getService(PlaceholderYamlLoaderService.class);
    this.resolveOptions = core.plugin().services().getService(PlaceholderResolveOptionsService.class);
    this.logger = core.plugin().getLogger();
  }

  @Override
  public void reloadAll() {
    List<PlaceholderFileModel> files = loader.loadAll();

    for (PlaceholderFileModel file : files) {
      if (file == null) continue;

      placeholders.unregisterOwner(file.owner());

      if (!file.enabled()) continue;

      Duration defaultTtl = file.defaultTtl() == null ? Duration.ZERO : file.defaultTtl();
      var options = resolveOptions.options();

      for (PlaceholderFileModel.Entry e : file.placeholders()) {
        if (e == null) continue;

        Duration ttl = e.ttlOverride() == null ? defaultTtl : e.ttlOverride();
        String expr = e.value() == null ? "" : e.value();

        placeholders.register(file.owner(), file.identifier(), e.id(), ctx -> {
          PlaceholderResolveContext scoped = (ctx == null)
              ? PlaceholderResolveContext.of(null, file.identifier())
              : new PlaceholderResolveContext(ctx.logicContext(), file.identifier(), ctx.cacheScopeKey(), ctx.variables());

          String expanded = placeholders.resolveText(expr, scoped, options);
          return new EvalExExpressionPlaceholderProvider(expanded).resolve(scoped);
        }, ttl);
      }

      logger.info("Loaded placeholders: " + file.debug());
    }
  }
}