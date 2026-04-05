package io.nexstudios.nexlogic.bukkit.effects.effects;

import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextKeys;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.MainThreadExecutorService;
import io.nexstudios.nexlogic.bukkit.services.expression.ExpressionService;
import io.nexstudios.nexlogic.bukkit.services.items.ItemProviderService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.runtime.EffectInstance;
import io.nexstudios.nexlogic.common.effects.types.EffectTypeService;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Dependencies({
    MainThreadExecutorService.class,
    LoggerService.class,
    ExpressionService.class,
    ItemProviderService.class
})
public final class GiveItemEffectType implements EffectTypeService {

  private final MainThreadExecutorService mainThread;
  private final LoggerService loggerService;
  private final ExpressionService expressions;
  private final ItemProviderService itemProviderService;

  public GiveItemEffectType(ServiceAccessor accessor) {
    this.mainThread = accessor.getService(MainThreadExecutorService.class);
    this.loggerService = accessor.getService(LoggerService.class);
    this.expressions = accessor.getService(ExpressionService.class);
    this.itemProviderService = accessor.getService(ItemProviderService.class);
  }

  @Override
  public String id() {
    return "give-item";
  }

  @Override
  public EffectInstance create(ConfigSection args) {
    final String itemIdRaw = args == null ? "" : firstNonBlank(
        args.getString("item", ""),
        args.getString("material", "")
    );
    final String amountExpr = args == null ? "1" : args.getString("amount", "1");

    final ItemStack baseItem = itemProviderService.getItem(itemIdRaw).orElse(null);

    // invalid -> no-op
    if (baseItem == null) {
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
      ItemStack itemToGive = baseItem.clone();
      itemToGive.setAmount(finalAmount);

      Runnable give = () -> {
        if (!player.isOnline()) return;
        player.getInventory().addItem(itemToGive);
      };

      if (mainThread.isMainThread()) give.run();
      else mainThread.execute(give);
    };
  }


  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  private static String firstNonBlank(String first, String second) {
    String normalizedFirst = nullToEmpty(first).trim();
    if (!normalizedFirst.isEmpty()) {
      return normalizedFirst;
    }

    return nullToEmpty(second).trim();
  }
}