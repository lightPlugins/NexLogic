package io.nexstudios.nexlogic.bukkit;

import io.nexstudios.menuservice.common.api.MenuDefinition;
import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.MenuService;
import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.builder.MenuDefinitionBuilder;
import io.nexstudios.menuservice.common.api.interaction.InteractionPolicies;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.nexstudios.menuservice.common.api.registry.DuplicateStrategy;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Beispiel-Menü:
 * - registriert eine MenuDefinition im MenuService-Registry
 * - kann per open(...) geöffnet werden
 *
 * Hinweis: Populator läuft bei dir async (thread-safe!), also keine Bukkit-API hier benutzen.
 */
public final class ExampleMainMenu {

  public static final MenuKey KEY = MenuKey.of("nexlogic", "main");

  private ExampleMainMenu() {}

  public static void register(@NotNull ServiceAccessor services) {
    Objects.requireNonNull(services, "services");

    // Je nach ServiceRegistry-API kann es statt require(...) auch get(...) / resolve(...) heißen.
    MenuService menuService = services.getService(MenuService.class);

    MenuDefinition def = MenuDefinitionBuilder.create()
        .key(KEY)
        .title("Hauptmenü")
        .rows(3)
        .interactionPolicy(InteractionPolicies.locked())
        .populator(ctx -> {
          // Slot 13 (Mitte bei 3 Reihen)
          ctx.slot(13).setItem(
              MenuItem.builder("minecraft:compass")
                  .displayName("Navigator")
                  .addLoreLine("Klick mich")
                  .build()
          );

          // Click-Handler: setzt das Item direkt im Slot um (kein Bukkit-Code!)
          ctx.slot(13).onClick(click -> {
            click.cancel();

            // kleines visuelles Feedback: Item im Slot umschalten
            if (click.action().name().contains("RIGHT")) {
              click.setCurrentItem(
                  MenuItem.builder("minecraft:lime_dye")
                      .displayName("Rechtsklick erkannt")
                      .addLoreLine("Slot: " + click.slot())
                      .build()
              );
            } else {
              click.setCurrentItem(
                  MenuItem.builder("minecraft:paper")
                      .displayName("Linksklick erkannt")
                      .addLoreLine("Viewer: " + click.viewer().name())
                      .build()
              );
            }
          });
        })
        .build();

    // Registrieren (FAIL wirft Exception bei doppeltem Key)
    menuService.registry().register(def, DuplicateStrategy.REPLACE);
  }

  public static void open(@NotNull ServiceAccessor services, @NotNull ViewerRef viewer) {
    Objects.requireNonNull(services, "services");
    Objects.requireNonNull(viewer, "viewer");

    MenuService menuService = services.getService(MenuService.class);
    menuService.open(viewer, KEY);
  }
}