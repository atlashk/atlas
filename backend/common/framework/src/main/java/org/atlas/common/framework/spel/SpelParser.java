package org.atlas.common.framework.spel;

import java.lang.reflect.Method;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
public class SpelParser implements InitializingBean {

  private SpelExpressionParser spelExpressionParser;
  private DefaultParameterNameDiscoverer nameDiscoverer;

  @Override
  public void afterPropertiesSet() {
    spelExpressionParser = new SpelExpressionParser();
    nameDiscoverer = new DefaultParameterNameDiscoverer();
  }

  /**
   * Parses and evaluates a SpEL (Spring Expression Language) expression within the context of a
   * method call. Method parameters are bound into the evaluation context so they can be referenced
   * inside the expression (e.g., "#id", "#name").
   *
   * @param spelExpression the SpEL expression to evaluate (e.g., "#id", "'user:' + #id")
   * @param method         the method being invoked, used to resolve parameter names
   * @param args           the actual argument values passed to the method
   * @return the evaluated expression result as a String
   */
  public String parse(String spelExpression, Method method, Object[] args) {
    // Prepare evaluation context with method parameters
    EvaluationContext context = new StandardEvaluationContext();
    String[] paramNames = nameDiscoverer.getParameterNames(method);
    if (paramNames != null) {
      for (int i = 0; i < paramNames.length; i++) {
        context.setVariable(paramNames[i], args[i]);
      }
    }

    Expression expression = spelExpressionParser.parseExpression(spelExpression);
    return expression.getValue(context, String.class);
  }
}
