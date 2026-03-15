package io.nexstudios.nexlogic.bukkit.effects.effects;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextKeys;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.MainThreadExecutorService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.runtime.EffectInstance;
import io.nexstudios.nexlogic.common.effects.types.EffectTypeService;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.serviceregistry.di.Dependencies;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

@Dependencies({
    MainThreadExecutorService.class,
    LoggerService.class
})
public final class GiveItemEffectType implements EffectTypeService {

  private final MainThreadExecutorService mainThread;
  private final LoggerService loggerService;

  public GiveItemEffectType(PaperPluginService core) {
    this.mainThread = core.plugin().services().getService(MainThreadExecutorService.class);
    this.loggerService = core.plugin().services().getService(LoggerService.class);
  }

  @Override
  public String id() {
    return "give-item";
  }

  @Override
  public EffectInstance create(ConfigSection args) {
    final String materialRaw = args == null ? "" : nullToEmpty(args.getString("material", ""));
    int amount = args == null ? 1 : args.getInt("amount", 1);

    if(amount <= 0 || amount > 99) {
      loggerService.logger().severe("Invalid amount provided: " + amount + " in effect: " + id());
      loggerService.logger().severe("Amount must be between 1 and 99");
      loggerService.logger().severe("Falling back to default amount of 1");
      amount = 1;
    }

    final Material material = parseMaterial(materialRaw);

    // invalid -> no-op
    if (material == null || material == Material.AIR) {
      return ctx -> {
        // no-op
      };
    }

    int finalAmount = amount;
    return ctx -> {
      if (ctx == null) return;

      Player player = ctx.get(BukkitContextKeys.PLAYER).orElse(null);
      if (player == null || !player.isOnline()) return;

      Runnable give = () -> {
        if (!player.isOnline()) return;

        // raw: rely on Bukkit to stack / place into free slots, no overflow handling here
        player.getInventory().addItem(new ItemStack(material, finalAmount));
      };

      if (mainThread.isMainThread()) give.run();
      else mainThread.execute(give);
    };
  }

  private static Material parseMaterial(String in) {
    String s = nullToEmpty(in).trim();
    if (s.isEmpty()) return Material.AIR;

    // accept "minecraft:stone" as well as "STONE"
    int idx = s.indexOf(':');
    if (idx >= 0 && idx + 1 < s.length()) s = s.substring(idx + 1);

    s = s.trim().toUpperCase(Locale.ROOT);

    Material m = Material.matchMaterial(s);
    return m == null ? Material.AIR : m;
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }
}