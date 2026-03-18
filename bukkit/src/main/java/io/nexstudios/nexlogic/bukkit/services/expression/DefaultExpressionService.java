package io.nexstudios.nexlogic.bukkit.services.expression;

import com.ezylang.evalex.Expression;
import io.nexstudios.nexlogic.bukkit.services.placeholder.runtime.PlaceholderRuntimeService;
import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.nexlogic.common.services.logging.LoggerService;
import io.nexstudios.serviceregistry.di.Dependencies;
import io.nexstudios.serviceregistry.di.ServiceAccessor;

import java.math.BigDecimal;

@Dependencies({
    PlaceholderRuntimeService.class,
    LoggerService.class
})
public class DefaultExpressionService implements ExpressionService {

  private final PlaceholderRuntimeService placeholders;
  private final LoggerService logger;

  public DefaultExpressionService(ServiceAccessor services) {
    this.placeholders = services.getService(PlaceholderRuntimeService.class);
    this.logger = services.getService(LoggerService.class);
  }

  @Override
  public double evaluate(String expression, LogicContext ctx) {
    if (expression == null || expression.isBlank()) {
      // Always return 0 if the expression is null or blank
      logger.logger().warning("Invalid expression: " + expression + " (context: " + ctx + ")");
      return 0.0d;
    }

    try {
      // 1) Resolve internal placeholders first (e.g. "%nexlogic:some_placeholder%")
      String expanded = placeholders.resolve(expression, ctx);

      if (expanded == null || expanded.isBlank()) {
        logger.logger().warning("Failed to resolve placeholders in expression: " + expression + " (context: " + ctx + ")");
        return 0.0d;
      }

      // 2) Evaluate the fully expanded expression via EvalEx
      BigDecimal value = new Expression(expanded).evaluate().getNumberValue();
      return value.doubleValue();
    } catch (Exception ex) {
      // Fail-safe: return 0 if placeholder resolving or evaluation fails
      logger.logger().warning("Failed to evaluate expression: " + expression + " (context: " + ctx + ")");
      // remove printStackTrace() in production
      ex.printStackTrace();
      return 0.0d;
    }
  }
}