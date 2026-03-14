package io.nexstudios.nexlogic.bukkit;

import io.nexstudios.itemservice.bukkit.service.item.ItemService;
import io.nexstudios.menuservice.common.api.MenuDefinition;
import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.MenuService;
import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.builder.MenuDefinitionBuilder;
import io.nexstudios.menuservice.common.api.interaction.InteractionPolicies;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.nexstudios.menuservice.common.api.registry.DuplicateStrategy;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Dependencies({
    ItemService.class,
    MenuService.class
})
public final class ExampleMainMenu {

  public static final MenuKey KEY = MenuKey.of("nexlogic", "main");

  private ExampleMainMenu() {}

  public static void register(@NotNull ServiceAccessor services) {
    Objects.requireNonNull(services, "services");

    MenuService menuService = services.getService(MenuService.class);
    ItemService items = services.getService(ItemService.class);

    MenuDefinition def = MenuDefinitionBuilder.create()
        .key(KEY)
        .title("Hauptmenü")
        .rows(3)
        .interactionPolicy(InteractionPolicies.locked())
        .populator(ctx -> {
          // Slot 13 (Mitte bei 3 Reihen)
          ctx.slot(13).setPlannedItem(() -> {
            ItemStack stack = items.builder(Material.COMPASS)
                .amount(1)
                .name(Component.text("Navigator", NamedTextColor.WHITE))
                .lore(l -> l
                    .line("&7Klick mich")
                )
                .build();

            return MenuItem.of(stack);
          });

          // Click-Handler läuft auf dem Main-Thread -> ItemService/ItemStack ok
          ctx.slot(13).onClick(click -> {
            click.cancel();

            if (click.action().name().contains("RIGHT")) {
              ItemStack stack = items.builder(Material.LIME_DYE)
                  .amount(1)
                  .name(Component.text("Rechtsklick erkannt", NamedTextColor.GREEN))
                  .lore(l -> l
                      .line("&7Slot: &f" + click.slot())
                  )
                  .build();

              click.setCurrentItem(MenuItem.of(stack));
              return;
            }

            ItemStack stack = items.builder(Material.PAPER)
                .amount(1)
                .name(Component.text("Linksklick erkannt", NamedTextColor.WHITE))
                .lore(l -> l
                    .line("&7Viewer: &f" + click.viewer().name())
                )
                .build();

            click.setCurrentItem(MenuItem.of(stack));
          });
        })
        .build();

    menuService.registry().register(def, DuplicateStrategy.REPLACE);
  }

  public static void open(@NotNull ServiceAccessor services, @NotNull ViewerRef viewer) {
    Objects.requireNonNull(services, "services");
    Objects.requireNonNull(viewer, "viewer");

    MenuService menuService = services.getService(MenuService.class);
    menuService.open(viewer, KEY);
  }
}