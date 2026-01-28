package org.atlas.common.framework.saga.core.compensation;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.common.framework.saga.core.annotation.SagaCompensationHandler;
import org.atlas.common.framework.saga.core.exception.SagaConfigException;
import org.atlas.common.framework.saga.core.messaging.SagaMessagePublisher;
import org.atlas.common.framework.saga.core.messaging.payload.SagaCompensation;
import org.atlas.common.framework.saga.core.messaging.payload.SagaCompensationReply;
import org.atlas.common.framework.error.ErrorUtil;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Unified dispatcher for {@link SagaCompensation} that routes events to
 * {@link SagaCompensationHandler} methods and publishes {@link SagaCompensationReply} after
 * execution.
 */
@Component
@ConditionalOnBean(SagaMessagePublisher.class)
@RequiredArgsConstructor
@Slf4j
public class SagaCompensationDispatcher {

  private final SagaMessagePublisher sagaMessagePublisher;
  private final ApplicationContext applicationContext;

  // One compensation handler per command
  private final Map<String, CachedHandlerMethod> cachedHandlerMethods = new ConcurrentHashMap<>();
  private volatile boolean initialized = false;

  /**
   * Initializes the compensation handler methods by scanning all beans for
   * {@link SagaCompensationHandler} annotations. Uses double-checked locking for thread safety.
   */
  private void initializeHandlers() {
    if (!initialized) {
      synchronized (this) {
        if (!initialized) {
          doInitializeHandlers();
          initialized = true;
        }
      }
    }
  }

  private void doInitializeHandlers() {
    String[] beanNames = applicationContext.getBeanDefinitionNames();

    for (String beanName : beanNames) {
      Object bean = applicationContext.getBean(beanName);
      Class<?> beanClass = AopUtils.getTargetClass(bean);

      for (Method method : beanClass.getDeclaredMethods()) {
        if (method.isAnnotationPresent(SagaCompensationHandler.class)) {
          SagaCompensationHandler annotation = method.getAnnotation(
              SagaCompensationHandler.class);
          String sagaCommandName = annotation.command();

          // Check if compensation handler already exists for this command name
          if (cachedHandlerMethods.containsKey(sagaCommandName)) {
            throw new SagaConfigException(
                "Multiple compensation handlers found for command: " + sagaCommandName +
                    ". Only one compensation handler per command is supported.");
          }

          // Validate method parameters and determine if it takes a parameter
          boolean takesParameter = validateAndCheckParameters(method);
          CachedHandlerMethod cachedHandlerMethod = new CachedHandlerMethod(bean, method,
              takesParameter);
          cachedHandlerMethods.put(sagaCommandName, cachedHandlerMethod);
          log.debug("Registered compensation handler {}.{} for command {}",
              beanClass.getSimpleName(), method.getName(), sagaCommandName);
        }
      }
    }
  }

  /**
   * Dispatches a {@link SagaCompensation} to the appropriate compensation handler method and
   * publishes reply event.
   */
  public void dispatch(SagaCompensation sagaCompensation) {
    initializeHandlers();

    CachedHandlerMethod cachedHandlerMethod = cachedHandlerMethods.get(
        sagaCompensation.getSagaCommandName());
    if (cachedHandlerMethod == null) {
      log.warn("No compensation handler found for saga command {}",
          sagaCompensation.getSagaCommandName());
      return;
    }

    SagaCompensationResult sagaCompensationResult;
    try {
      log.debug("Dispatching saga compensation {} to handler {}",
          sagaCompensation.getSagaCommandName(),
          cachedHandlerMethod.methodSignature);
      sagaCompensationResult = (SagaCompensationResult) cachedHandlerMethod.invoke(
          sagaCompensation);
      if (sagaCompensationResult.isSuccess()) {
        log.info("Successfully executed saga compensation handler {}",
            cachedHandlerMethod.methodSignature);
      } else {
        log.error("Failed to execute saga compensation handler {}: {}",
            cachedHandlerMethod.methodSignature, sagaCompensationResult.getError());
      }
    } catch (Exception e) {
      Throwable cause = ErrorUtil.getRootCause(e);
      sagaCompensationResult = SagaCompensationResult.failure(cause);
      log.error("Failed to execute saga compensation handler {}: {}",
          cachedHandlerMethod.methodSignature, sagaCompensationResult.getError(), cause);
    }

    // Publish compensation reply
    SagaCompensationReply reply = SagaCompensationReply.builder()
        .sagaId(sagaCompensation.getSagaId())
        .sagaName(sagaCompensation.getSagaName())
        .sagaCommandName(sagaCompensation.getSagaCommandName())
        .result(sagaCompensationResult)
        .build();
    sagaMessagePublisher.publish(reply);
  }

  /**
   * Validates method parameters and returns whether the method takes a parameter. Compensation
   * handler methods can accept only one optional parameter: {@link SagaCompensation}.
   */
  private boolean validateAndCheckParameters(Method method) {
    Parameter[] parameters = method.getParameters();

    // No parameters
    if (parameters.length == 0) {
      return false;
    }

    // Validate that there's only one parameter and it's SagaCompensation
    if (parameters.length > 1) {
      throw new SagaConfigException(
          String.format(
              "Compensation handler method %s.%s can accept at most one parameter of type SagaCompensation, but found %d parameters",
              method.getDeclaringClass().getSimpleName(), method.getName(), parameters.length));
    }

    Parameter parameter = parameters[0];
    if (parameter.getType() != SagaCompensation.class) {
      throw new SagaConfigException(
          String.format(
              "Compensation handler method %s.%s parameter must be of type SagaCompensation, but found: %s",
              method.getDeclaringClass().getSimpleName(), method.getName(),
              parameter.getType().getName()));
    }

    return true;
  }

  /**
   * Cached compensation handler method with simplified parameter handling.
   */
  @Getter
  private static class CachedHandlerMethod {

    private final Object bean;
    private final Method method;
    private final boolean takesParameter;
    private final String methodSignature;

    public CachedHandlerMethod(Object bean, Method method, boolean takesParameter) {
      this.bean = bean;
      this.method = method;
      this.takesParameter = takesParameter;
      this.methodSignature = method.getDeclaringClass().getSimpleName() + "." + method.getName();
      // Optimize reflection access
      method.setAccessible(true);
    }

    public Object invoke(SagaCompensation sagaCompensation) throws Exception {
      if (takesParameter) {
        return method.invoke(bean, sagaCompensation);
      } else {
        return method.invoke(bean);
      }
    }
  }
}
