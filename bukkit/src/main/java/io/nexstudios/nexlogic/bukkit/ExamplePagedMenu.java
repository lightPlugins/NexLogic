package io.nexstudios.nexlogic.bukkit;

import io.nexstudios.menuservice.common.api.MenuDefinition;
import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.MenuService;
import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.builder.MenuDefinitionBuilder;
import io.nexstudios.menuservice.common.api.interaction.InteractionPolicies;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.nexstudios.menuservice.common.api.page.PageAlignment;
import io.nexstudios.menuservice.common.api.page.PageBounds;
import io.nexstudios.menuservice.common.api.page.PageClickHandler;
import io.nexstudios.menuservice.common.api.page.PageItemRenderer;
import io.nexstudios.menuservice.common.api.page.PageNavigation;
import io.nexstudios.menuservice.common.api.page.PageSource;
import io.nexstudios.menuservice.common.api.page.PagedAreaDefinition;
import io.nexstudios.menuservice.common.api.registry.DuplicateStrategy;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public final class ExamplePagedMenu {

  public static final MenuKey KEY = MenuKey.of("nexlogic", "paged");

  private static final AtomicLong RENDER_TICK = new AtomicLong(0L);

  private ExamplePagedMenu() {}

  public static void register(@NotNull ServiceAccessor services) {
    Objects.requireNonNull(services, "services");

    MenuService menuService = services.getService(MenuService.class);

    PagedAreaDefinition<String> pagedArea = getStringPagedAreaDefinition();

    MenuDefinition def = MenuDefinitionBuilder.create()
        .key(KEY)
        .title("Paged Inventory (Lore Test)")
        .rows(6)
        .refreshInterval(Duration.ofSeconds(1))
        .interactionPolicy(InteractionPolicies.locked())
        .populator(ctx -> {
          long tick = RENDER_TICK.incrementAndGet();

          ctx.slot(4).setItem(
              MenuItem.builder("minecraft:clock")
                  .displayName("§bRender-Tick")
                  .lore(List.of(
                      "§7Tick: §e" + tick,
                      "§7Wenn das hochzählt: Refresh + Lore-Update OK"
                  ))
                  .build()
          );
        })
        .addPagedArea(pagedArea)
        .build();

    menuService.registry().register(def, DuplicateStrategy.REPLACE);
  }

  private static @NotNull PagedAreaDefinition<String> getStringPagedAreaDefinition() {
    PageSource<String> source = (MenuKey menuKey, ViewerRef viewer) -> {
      List<String> out = new ArrayList<>(100);
      for (int i = 1; i <= 100; i++) {
        out.add("Eintrag #" + i);
      }
      return List.copyOf(out);
    };

    PageItemRenderer<String> renderer = (element, index) -> {
      long tick = RENDER_TICK.get(); // wird im populator() pro Render erhöht

      return MenuItem.builder("minecraft:paper")
          .displayName("§f" + element)
          .lore(List.of(
              "§7Index: §f" + index,
              "§7Tick: §e" + tick,
              "§8(soll jede Sekunde hochzählen)"
          ))
          .build();
    };

    return getStringPagedAreaDefinition(source, renderer);
  }

  private static @NotNull PagedAreaDefinition<String> getStringPagedAreaDefinition(PageSource<String> source, PageItemRenderer<String> renderer) {
    PageClickHandler<String> clickHandler = (element, index, ctx) -> {
      ctx.cancel();
      ctx.setCurrentItem(
          MenuItem.builder("minecraft:lime_dye")
              .displayName("§aAusgewählt: §f" + element)
              .lore(List.of(
                  "§7Globaler Index: §f" + index,
                  "§7Tick: §e" + RENDER_TICK.get()
              ))
              .build()
      );
    };

    PageBounds bounds = new PageBounds(1, 1, 7, 4, PageAlignment.LEFT);

    PageNavigation nav = new PageNavigation(
        OptionalInt.of(45),
        OptionalInt.of(53),
        OptionalInt.of(49)
    );

    return new PagedAreaDefinition<>(
        "entries",
        bounds,
        source,
        renderer,
        nav,
        Optional.of(clickHandler)
    );
  }

  public static void open(@NotNull ServiceAccessor services, @NotNull ViewerRef viewer) {
    Objects.requireNonNull(services, "services");
    Objects.requireNonNull(viewer, "viewer");

    MenuService menuService = services.getService(MenuService.class);
    menuService.open(viewer, KEY);
  }
}