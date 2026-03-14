package io.nexstudios.nexlogic.bukkit;

import io.nexstudios.itemservice.bukkit.service.item.ItemService;
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
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicLong;

@Dependencies({
    ItemService.class,
    MenuService.class
})
public final class ExamplePagedMenu {

  public static final MenuKey KEY = MenuKey.of("nexlogic", "paged");

  private static final AtomicLong RENDER_TICK = new AtomicLong(0L);

  private ExamplePagedMenu() {}

  public static void register(@NotNull ServiceAccessor services) {
    Objects.requireNonNull(services, "services");

    MenuService menuService = services.getService(MenuService.class);
    ItemService items = services.getService(ItemService.class);

    PagedAreaDefinition<String> pagedArea = getStringPagedAreaDefinition(items);

    MenuDefinition def = MenuDefinitionBuilder.create()
        .key(KEY)
        .title("Paged Inventory (Lore Test)")
        .rows(6)
        .refreshInterval(Duration.ofSeconds(1))
        .interactionPolicy(InteractionPolicies.locked())
        .populator(ctx -> {
          long tick = RENDER_TICK.incrementAndGet();

          // Async-safe: ItemStack/MenuItem erst im Supplier (Main-Thread) erzeugen
          ctx.slot(4).setPlannedItem(() -> MenuItem.of(
              items.builder(Material.CLOCK)
                  .amount(1)
                  .name(Component.text("Render-Tick", NamedTextColor.AQUA))
                  .lore(l -> l
                      .line("&7Tick: &e" + tick)
                      .line("&7Wenn das hochzählt: Refresh + Lore-Update OK")
                  )
                  .build()
          ));
        })
        .addPagedArea(pagedArea)
        .build();

    menuService.registry().register(def, DuplicateStrategy.REPLACE);
  }

  private static @NotNull PagedAreaDefinition<String> getStringPagedAreaDefinition(ItemService items) {
    PageSource<String> source = (MenuKey menuKey, ViewerRef viewer) -> {
      List<String> out = new ArrayList<>(100);
      for (int i = 1; i <= 100; i++) {
        out.add("Eintrag #" + i);
      }
      return List.copyOf(out);
    };

    // NEU: PageItemRenderer liefert MenuItemSupplier (deferred)
    PageItemRenderer<String> renderer = (element, index) -> () -> {
      long tick = RENDER_TICK.get(); // wird im populator() pro Render erhöht

      ItemStack stack = items.builder(Material.PAPER)
          .amount(1)
          .name(Component.text(element, NamedTextColor.WHITE))
          .lore(l -> l
              .line("&7Index: &f" + index)
              .line("&7Tick: &e" + tick)
              .line("&8(soll jede Sekunde hochzählen)")
          )
          .build();

      return MenuItem.of(stack);
    };

    return getStringPagedAreaDefinition(items, source, renderer);
  }

  private static @NotNull PagedAreaDefinition<String> getStringPagedAreaDefinition(
      ItemService items,
      PageSource<String> source,
      PageItemRenderer<String> renderer
  ) {
    PageClickHandler<String> clickHandler = (element, index, ctx) -> {
      // Click handler läuft auf Main-Thread -> hier darfst du Items direkt bauen
      ctx.cancel();

      ItemStack stack = items.builder(Material.LIME_DYE)
          .amount(1)
          .name(Component.text("Ausgewählt: " + element, NamedTextColor.GREEN))
          .lore(l -> l
              .line("&7Globaler Index: &f" + index)
              .line("&7Tick: &e" + RENDER_TICK.get())
          )
          .build();

      ctx.setCurrentItem(MenuItem.of(stack));
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