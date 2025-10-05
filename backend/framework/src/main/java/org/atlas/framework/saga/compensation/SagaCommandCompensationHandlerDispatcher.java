package org.atlas.framework.saga.compensation;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.annotation.SagaCommandCompensationHandler;
import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.saga.event.SagaCommandCompensationEvent;
import org.atlas.framework.saga.event.SagaCommandCompensationReplyEvent;
import org.atlas.framework.saga.event.SagaEventPublisher;
import org.atlas.framework.saga.exception.SagaConfigException;
import org.atlas.framework.saga.exception.SagaNotFoundException;
import org.atlas.framework.saga.repository.SagaRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Unified dispatcher for SagaCommandCompensationEvent that routes events to
 *
 * @SagaCommandCompensationHandler methods and publishes SagaCommandCompensationReplyEvent after
 * execution.
 *
 * <p>This component combines compensation event dispatching and reply publishing in a single
 * component:
 * <ul>
 *   <li>Routes incoming SagaCommandCompensationEvent to the appropriate compensation handler method</li>
 *   <li>Executes the compensation handler with simplified argument resolution</li>
 *   <li>Publishes SagaCommandCompensationReplyEvent with success/failure status</li>
 *   <li>Supports exactly one compensation handler per command for simplified architecture</li>
 * </ul>
 *
 * <p><strong>Compensation Handler Method Constraints:</strong>
 * <ul>
 *   <li>Compensation handler methods can accept at most one parameter</li>
 *   <li>The parameter must be of type {@link SagaContext} (optional)</li>
 *   <li>Methods with no parameters are also supported</li>
 * </ul>
 * @see org.atlas.framework.saga.annotation.SagaCommandCompensationHandler
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCommandCompensationHandlerDispatcher implements InitializingBean {

  private final SagaRepository sagaRepository;
  private final SagaEventPublisher sagaEventPublisher;
  private final ApplicationContext applicationContext;

  // One compensation handler per command
  private final Map<String, CachedHandlerMethod> cachedHandlerMethods = new HashMap<>();

  /**
   * Initializes the compensation handler methods by scanning all beans for
   *
   * @SagaCommandCompensationHandler annotations.
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    Map<String, Object> beans = applicationContext.getBeansOfType(Object.class);

    for (Object bean : beans.values()) {
      Class<?> beanClass = bean.getClass();
      Method[] methods = beanClass.getDeclaredMethods();

      for (Method method : methods) {
        if (method.isAnnotationPresent(SagaCommandCompensationHandler.class)) {
          SagaCommandCompensationHandler annotation = method.getAnnotation(
              SagaCommandCompensationHandler.class);
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
   * Dispatches a SagaCommandCompensationEvent to the appropriate compensation handler method and
   * publishes reply event.
   */
  public void dispatch(SagaCommandCompensationEvent event) {
    CachedHandlerMethod cachedHandlerMethod = cachedHandlerMethods.get(event.getSagaCommandName());
    if (cachedHandlerMethod == null) {
      log.warn("No compensation handler found for saga command {}", event.getSagaCommandName());
      return;
    }

    SagaEntity sagaEntity = sagaRepository.findById(event.getSagaId())
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + event.getSagaId()));

    log.debug("Dispatching saga command compensation {} to handler {}", event.getSagaCommandName(),
        cachedHandlerMethod.methodSignature);

    SagaContext sagaContext = SagaContext.deserialize(event.getSagaContext());
    Exception exception = null;

    try {
      // Not handle method result here
      cachedHandlerMethod.invoke(sagaContext);
      log.debug("Successfully executed compensation handler {}",
          cachedHandlerMethod.methodSignature);
    } catch (Exception e) {
      exception = e;
      log.error("Failed to execute compensation handler {}", cachedHandlerMethod.methodSignature,
          e);
    }

    // Publish compensation reply event
    publishSagaCommandCompensationReplyEvent(sagaEntity, event.getSagaCommandName(), exception);
  }

  /**
   * Publishes SagaCommandCompensationReplyEvent with the result or exception.
   */
  private void publishSagaCommandCompensationReplyEvent(SagaEntity sagaEntity,
      String sagaCommandName, Exception exception) {
    SagaCommandCompensationReplyEvent replyEvent;

    if (exception != null) {
      replyEvent = SagaCommandCompensationReplyEvent.failure(sagaEntity, sagaCommandName,
          exception);
      log.debug("Publishing event for saga command compensation failure reply: {}", replyEvent);
    } else {
      replyEvent = SagaCommandCompensationReplyEvent.success(sagaEntity, sagaCommandName);
      log.debug("Publishing event for saga command compensation success reply: {}", replyEvent);
    }

    sagaEventPublisher.publish(replyEvent);
  }

  /**
   * Validates method parameters and returns whether the method takes a parameter. Compensation
   * handler methods can accept only one optional parameter: SagaContext.
   */
  private boolean validateAndCheckParameters(Method method) {
    Parameter[] parameters = method.getParameters();

    // No parameters
    if (parameters.length == 0) {
      return false;
    }

    // Validate that there's only one parameter and it's SagaContext
    if (parameters.length > 1) {
      throw new SagaConfigException(
          String.format(
              "Compensation handler method %s.%s can accept at most one parameter of type SagaContext, but found %d parameters",
              method.getDeclaringClass().getSimpleName(), method.getName(), parameters.length));
    }

    Parameter parameter = parameters[0];
    if (parameter.getType() != SagaContext.class) {
      throw new SagaConfigException(
          String.format(
              "Compensation handler method %s.%s parameter must be of type SagaContext, but found: %s",
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

    public Object invoke(SagaContext sagaContext) throws Exception {
      if (takesParameter) {
        return method.invoke(bean, sagaContext);
      } else {
        return method.invoke(bean);
      }
    }
  }
}
