package io.nexstudios.nexlogic.bukkit;

import io.nexstudios.itemservice.bukkit.service.item.ItemService;
import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.MenuService;
import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.builder.MenuDefinitionBuilder;
import io.nexstudios.menuservice.common.api.interaction.InteractionPolicies;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.nexstudios.menuservice.common.api.page.PageAlignment;
import io.nexstudios.menuservice.common.api.page.PageBounds;
import io.nexstudios.menuservice.common.api.page.PageNavigation;
import io.nexstudios.menuservice.common.api.page.PageSource;
import io.nexstudios.menuservice.common.api.page.PagedAreaDefinition;
import io.nexstudios.menuservice.common.api.page.control.PageControlButton;
import io.nexstudios.menuservice.common.api.page.control.PageFilterControl;
import io.nexstudios.menuservice.common.api.page.control.PageSortControl;
import io.nexstudios.menuservice.common.api.registry.DuplicateStrategy;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;

@Dependencies({
    ItemService.class,
    MenuService.class
})
public final class ExampleControlsPaged {

  public static final MenuKey KEY = MenuKey.of("nexlogic", "controls_paged");

  private static final String AREA_ID = "entries";

  private static final int SLOT_FILTER_BUTTON = 0;
  private static final int SLOT_SORT_BUTTON = 8;

  private static final String FILTER_ID = "filter";
  private static final String SORT_ID = "sort";

  private ExampleControlsPaged() {}

  public static void register(@NotNull ServiceAccessor services) {
    Objects.requireNonNull(services, "services");

    MenuService menuService = services.getService(MenuService.class);
    ItemService items = services.getService(ItemService.class);

    var def = MenuDefinitionBuilder.create()
        .key(KEY)
        .title("Test: Paging + Filter + Sort")
        .rows(6)
        .refreshInterval(Duration.ofSeconds(1))
        .interactionPolicy(InteractionPolicies.locked())
        .populator(ctx -> {
          // WICHTIG: Populator läuft async -> nur planned items setzen
          ctx.slot(SLOT_FILTER_BUTTON).setPlannedItem(() -> MenuItem.of(buildFilterButton(items)));
          ctx.slot(SLOT_SORT_BUTTON).setPlannedItem(() -> MenuItem.of(buildSortButton(items)));

          // kleine Erklärung
          ctx.slot(4).setPlannedItem(() -> MenuItem.of(
              items.builder(Material.BOOK)
                  .amount(1)
                  .name(Component.text("Info", NamedTextColor.YELLOW))
                  .lore(l -> l
                      .line("&7Links: Filter umschalten")
                      .line("&7Rechts: Sortierung umschalten")
                      .line("&7Unten: Seiten wechseln")
                  )
                  .build()
          ));
        })
        .addFilterControl(AREA_ID, buildFilterControl())
        .addSortControl(AREA_ID, buildSortControl())
        .addControlButton(buildFilterControlButton(items))
        .addControlButton(buildSortControlButton(items))
        .addPagedArea(buildPagedArea(items))
        .build();

    menuService.registry().register(def, DuplicateStrategy.REPLACE);
  }

  public static void open(@NotNull ServiceAccessor services, @NotNull ViewerRef viewer) {
    Objects.requireNonNull(services, "services");
    Objects.requireNonNull(viewer, "viewer");

    services.getService(MenuService.class).open(viewer, KEY);
  }

  // -----------------------------
  // Paging + data
  // -----------------------------

  private static PagedAreaDefinition<Entry> buildPagedArea(ItemService items) {
    PageSource<Entry> source = (menuKey, viewer) -> List.of(
        new Entry("Apple", Material.APPLE, Category.FOOD, 5),
        new Entry("Bread", Material.BREAD, Category.FOOD, 3),
        new Entry("Carrot", Material.CARROT, Category.FOOD, 2),
        new Entry("Cooked Beef", Material.COOKED_BEEF, Category.FOOD, 8),

        new Entry("Stone", Material.STONE, Category.BLOCK, 1),
        new Entry("Cobblestone", Material.COBBLESTONE, Category.BLOCK, 1),
        new Entry("Oak Planks", Material.OAK_PLANKS, Category.BLOCK, 2),
        new Entry("Glass", Material.GLASS, Category.BLOCK, 2),
        new Entry("Iron Block", Material.IRON_BLOCK, Category.BLOCK, 9),
        new Entry("Gold Block", Material.GOLD_BLOCK, Category.BLOCK, 12),

        new Entry("Diamond", Material.DIAMOND, Category.MISC, 50),
        new Entry("Emerald", Material.EMERALD, Category.MISC, 40),
        new Entry("Redstone", Material.REDSTONE, Category.MISC, 4),
        new Entry("Ender Pearl", Material.ENDER_PEARL, Category.MISC, 15),
        new Entry("Blaze Rod", Material.BLAZE_ROD, Category.MISC, 18)
    );

    // Bounds: 7x4 = 28 Items pro Seite
    PageBounds bounds = new PageBounds(1, 1, 7, 4, PageAlignment.LEFT);

    // Navigation unten: prev (45), refresh (49), next (53)
    PageNavigation nav = new PageNavigation(
        OptionalInt.of(45),
        OptionalInt.of(53),
        OptionalInt.of(49)
    );

    return new PagedAreaDefinition<>(
        AREA_ID,
        bounds,
        source,
        (entry, index) -> () -> MenuItem.of(renderEntry(items, entry, index)),
        nav,
        Optional.of((entry, index, ctx) -> {
          ctx.cancel();
          // click handler ist main-thread -> ok, sofort zu bauen
          ctx.setCurrentItem(MenuItem.of(
              items.builder(Material.LIME_DYE)
                  .amount(1)
                  .name(Component.text("Ausgewählt", NamedTextColor.GREEN))
                  .lore(l -> l
                      .line("&7Name: &f" + entry.name())
                      .line("&7Kategorie: &f" + entry.category().id)
                      .line("&7Wert: &e" + entry.value())
                      .line("&8Index: " + index)
                  )
                  .build()
          ));
        })
    );
  }

  private static ItemStack renderEntry(ItemService items, Entry e, int index) {
    return items.builder(e.material())
        .amount(1)
        .name(Component.text(e.name(), NamedTextColor.WHITE))
        .lore(l -> l
            .line("&7Kategorie: &f" + e.category().id)
            .line("&7Wert: &e" + e.value())
            .line("&8Index: " + index)
        )
        .build();
  }

  private record Entry(String name, Material material, Category category, int value) {}

  private enum Category {
    FOOD("Food"),
    BLOCK("Block"),
    MISC("Misc");

    final String id;
    Category(String id) { this.id = id; }
  }

  // -----------------------------
  // Filter Control
  // -----------------------------

  private static PageFilterControl<Entry> buildFilterControl() {
    return new PageFilterControl<>() {
      @Override public String controlId() { return FILTER_ID; }

      @Override public List<String> modeIds() { return List.of("all", "food", "blocks"); }

      @Override public String defaultModeId() { return "all"; }

      @Override
      public String labelForMode(String modeId) {
        return switch (modeId) {
          case "food" -> "Food";
          case "blocks" -> "Blocks";
          default -> "All";
        };
      }

      @Override
      public Predicate<Entry> predicateFor(String modeId, MenuKey menuKey, ViewerRef viewer) {
        return switch (modeId) {
          case "food" -> e -> e.category() == Category.FOOD;
          case "blocks" -> e -> e.category() == Category.BLOCK;
          default -> e -> true;
        };
      }
    };
  }

  private static PageControlButton buildFilterControlButton(ItemService items) {
    return new PageControlButton() {
      @Override public String areaId() { return AREA_ID; }
      @Override public String controlId() { return FILTER_ID; }
      @Override public int slot() { return SLOT_FILTER_BUTTON; }

      @Override
      public MenuItem render(RenderContext ctx) {
        String mode = ctx.activeModeId().orElse(ctx.control().defaultModeId());
        String label = ctx.control().labelForMode(mode);

        return MenuItem.of(items.builder(Material.HOPPER)
            .amount(1)
            .name(Component.text("Filter: " + label, NamedTextColor.AQUA))
            .lore(l -> l
                .line("&7Klick: Modus wechseln")
                .line("&8Aktiv: &f" + mode)
            )
            .build());
      }

      @Override
      public void onClick(ClickContext ctx) {
        ctx.stateStore().cycleToNextMode(ctx.viewer(), ctx.menuKey(), ctx.areaId(), ctx.control());
        ctx.requestAreaRefresh();
      }
    };
  }

  private static ItemStack buildFilterButton(ItemService items) {
    return items.builder(Material.HOPPER)
        .amount(1)
        .name(Component.text("Filter", NamedTextColor.AQUA))
        .lore(l -> l.line("&7Wird vom Control-Button gerendert"))
        .build();
  }

  // -----------------------------
  // Sort Control
  // -----------------------------

  private static PageSortControl<Entry> buildSortControl() {
    return new PageSortControl<>() {
      @Override public String controlId() { return SORT_ID; }

      @Override public List<String> modeIds() { return List.of("name_asc", "name_desc", "value_desc"); }

      @Override public String defaultModeId() { return "name_asc"; }

      @Override
      public String labelForMode(String modeId) {
        return switch (modeId) {
          case "name_desc" -> "Name Z-A";
          case "value_desc" -> "Value High-Low";
          default -> "Name A-Z";
        };
      }

      @Override
      public Comparator<Entry> comparatorFor(String modeId, MenuKey menuKey, ViewerRef viewer) {
        return switch (modeId) {
          case "name_desc" -> Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER).reversed();
          case "value_desc" -> Comparator.comparingInt(Entry::value).reversed()
              .thenComparing(Entry::name, String.CASE_INSENSITIVE_ORDER);
          default -> Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER);
        };
      }
    };
  }

  private static PageControlButton buildSortControlButton(ItemService items) {
    return new PageControlButton() {
      @Override public String areaId() { return AREA_ID; }
      @Override public String controlId() { return SORT_ID; }
      @Override public int slot() { return SLOT_SORT_BUTTON; }

      @Override
      public MenuItem render(RenderContext ctx) {
        String mode = ctx.activeModeId().orElse(ctx.control().defaultModeId());
        String label = ctx.control().labelForMode(mode);

        return MenuItem.of(items.builder(Material.COMPARATOR)
            .amount(1)
            .name(Component.text("Sort: " + label, NamedTextColor.GOLD))
            .lore(l -> l
                .line("&7Klick: Modus wechseln")
                .line("&8Aktiv: &f" + mode)
            )
            .build());
      }

      @Override
      public void onClick(ClickContext ctx) {
        ctx.stateStore().cycleToNextMode(ctx.viewer(), ctx.menuKey(), ctx.areaId(), ctx.control());
        ctx.requestAreaRefresh();
      }
    };
  }

  private static ItemStack buildSortButton(ItemService items) {
    return items.builder(Material.COMPARATOR)
        .amount(1)
        .name(Component.text("Sort", NamedTextColor.GOLD))
        .lore(l -> l.line("&7Wird vom Control-Button gerendert"))
        .build();
  }
}