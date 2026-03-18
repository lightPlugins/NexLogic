package io.nexstudios.nexlogic.bukkit.services.expression;

import io.nexstudios.nexlogic.common.effects.model.LogicContext;
import io.nexstudios.serviceregistry.di.Service;

public interface ExpressionService extends Service {

  /**
   * Evaluates the provided mathematical or logical expression, potentially utilizing contextual data
   * from the provided LogicContext.
   *
   * @param expression the expression to evaluate; must be a valid string representation of the expression.
   *                   If null or blank, a default value of 0 is returned.
   * @param ctx        the {@code LogicContext} providing relevant data to alter the evaluation. If null,
   *                   the evaluation runs without context.
   * @return the result of the expression evaluation as a double. If the expression is invalid or an error
   *         occurs during evaluation, 0 is returned as a fail-safe.
   */
  double evaluate(String expression, LogicContext ctx);

  /**
   * Evaluates the provided mathematical or logical expression without using any additional context.
   *
   * @param expression the expression to evaluate; must be a valid string representation of the expression.
   *                   If null or blank, a default value of 0 is returned.
   * @return the result of the expression evaluation as a double. If the expression is invalid or an error
   *         occurs during evaluation, 0 is returned as a fail-safe.
   */
  default double evaluate(String expression) {
    return evaluate(expression, null);
  }
}
