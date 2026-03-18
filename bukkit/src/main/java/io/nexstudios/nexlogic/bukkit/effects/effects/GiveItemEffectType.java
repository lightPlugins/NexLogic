package io.nexstudios.nexlogic.bukkit.effects.effects;

import io.nexstudios.framework.paper.services.plugin.PaperPluginService;
import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextKeys;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.MainThreadExecutorService;
import io.nexstudios.nexlogic.bukkit.services.expression.ExpressionService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.runtime.EffectInstance;
import io.nexstudios.nexlogic.common.effects.types.EffectTypeService;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

@Dependencies({
    MainThreadExecutorService.class,
    LoggerService.class,
    ExpressionService.class
})
public final class GiveItemEffectType implements EffectTypeService {

  private final MainThreadExecutorService mainThread;
  private final LoggerService loggerService;
  private final ExpressionService expressions;

  public GiveItemEffectType(ServiceAccessor accessor) {
    this.mainThread = accessor.getService(MainThreadExecutorService.class);
    this.loggerService = accessor.getService(LoggerService.class);
    this.expressions = accessor.getService(ExpressionService.class);
  }

  @Override
  public String id() {
    return "give-item";
  }

  @Override
  public EffectInstance create(ConfigSection args) {
    final String materialRaw = args == null ? "" : nullToEmpty(args.getString("material", ""));
    final String amountExpr = args == null ? "1" : args.getString("amount", "1");

    final Material material = parseMaterial(materialRaw);

    // invalid -> no-op
    if (material == null || material == Material.AIR) {
      return ctx -> {
        // no-op
      };
    }

    return ctx -> {
      if (ctx == null) return;

      Player player = ctx.get(BukkitContextKeys.PLAYER).orElse(null);
      if (player == null || !player.isOnline()) return;

      double rawAmount = expressions.evaluate(amountExpr, ctx);

      int amount = (int) Math.floor(rawAmount);
      if (amount <= 0 || amount > 99) {
        loggerService.logger().severe("Invalid amount provided: " + rawAmount + " in effect: " + id());
        loggerService.logger().severe("Amount must be between 1 and 99");
        loggerService.logger().severe("Falling back to default amount of 1");
        amount = 1;
      }

      int finalAmount = amount;
      Runnable give = () -> {
        if (!player.isOnline()) return;
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