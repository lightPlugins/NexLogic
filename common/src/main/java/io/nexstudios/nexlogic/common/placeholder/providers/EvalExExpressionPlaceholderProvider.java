package io.nexstudios.nexlogic.common.placeholder.providers;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import io.nexstudios.nexlogic.common.placeholder.PlaceholderProvider;
import io.nexstudios.nexlogic.common.placeholder.PlaceholderResolveContext;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class EvalExExpressionPlaceholderProvider implements PlaceholderProvider {

  private final String rawExpression;
  private final AtomicReference<Expression> compiled = new AtomicReference<>();

  public EvalExExpressionPlaceholderProvider(String rawExpression) {
    this.rawExpression = Objects.requireNonNull(rawExpression, "rawExpression");
  }

  @Override
  public String resolve(PlaceholderResolveContext ctx) {
    // We expect nested placeholders already expanded by the placeholder service.
    // This provider only evaluates the final expression string.
    try {
      Expression expr = compiled.updateAndGet(existing -> existing != null ? existing : new Expression(rawExpression));
      return expr.evaluate().getStringValue();
    } catch (ParseException | EvaluationException ex) {
      // Fail safe: if expression is invalid, return empty string
      return "0";
    } catch (RuntimeException ex) {
      // Don't let unexpected evalex/runtime issues break placeholder resolution.
      return "0";
    }
  }

  public String rawExpression() {
    return rawExpression;
  }
}