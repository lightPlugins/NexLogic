package io.nexstudios.nexlogic.bukkit.effects.effects;

import io.nexstudios.nexlogic.bukkit.services.effects.context.BukkitContextKeys;
import io.nexstudios.nexlogic.bukkit.services.effects.executor.MainThreadExecutorService;
import io.nexstudios.nexlogic.bukkit.services.heads.HeadService;
import io.nexstudios.nexlogic.common.effects.config.ConfigSection;
import io.nexstudios.nexlogic.common.effects.runtime.EffectInstance;
import io.nexstudios.nexlogic.common.effects.types.EffectTypeService;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.nexlogic.common.services.triggers.schema.ContextCapability;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.Set;

@Dependencies({
	HeadService.class,
	MainThreadExecutorService.class,
	LoggerService.class
})
public final class GiveHeadEffectType implements EffectTypeService {

  private final HeadService headService;
  private final MainThreadExecutorService mainThread;
  private final LoggerService logger;

  public GiveHeadEffectType(ServiceAccessor accessor) {
	this.headService = accessor.getService(HeadService.class);
	this.mainThread = accessor.getService(MainThreadExecutorService.class);
	this.logger = accessor.getService(LoggerService.class);
  }

  @Override
  public String id() {
	return "give-head";
  }

  @Override
  public Set<ContextCapability> requiredCapabilities() {
	return Set.of(ContextCapability.PLAYER);
  }

  @Override
  public EffectInstance create(ConfigSection args) {
	final UUID targetUuid = parseUuid(args == null ? null : args.getString("player-uuid", null));
	if (targetUuid == null) {
	  logger.logger().warning("The configured player UUID for the give-head effect is invalid or missing.");
	  return ctx -> {
		// no-op
	  };
	}

	return ctx -> {
	  if (ctx == null) {
		return;
	  }

	  Player player = ctx.get(BukkitContextKeys.PLAYER).orElse(null);
	  if (player == null || !player.isOnline()) {
		return;
	  }

	  headService.loadHead(targetUuid).whenComplete((head, throwable) -> {
		if (throwable != null) {
		  logger.logger().warning("Failed to load the head for UUID " + targetUuid + ": " + throwable.getMessage());
		  return;
		}

		if (head == null) {
		  logger.logger().warning("The head for UUID " + targetUuid + " could not be resolved.");
		  return;
		}

		giveHead(player, head);
	  });
	};
  }

  private void giveHead(Player player, ItemStack head) {
	Runnable task = () -> {
	  if (player == null || !player.isOnline()) {
		return;
	  }

	  player.getInventory().addItem(head.clone());
	};

	if (mainThread.isMainThread()) {
	  task.run();
	  return;
	}

	mainThread.execute(task);
  }

  private static UUID parseUuid(String raw) {
	if (raw == null || raw.isBlank()) {
	  return null;
	}

	try {
	  return UUID.fromString(raw.trim());
	} catch (IllegalArgumentException ignored) {
	  return null;
	}
  }
}
