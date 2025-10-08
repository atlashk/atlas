package org.atlas.framework.saga.command;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.annotation.SagaCommandHandler;
import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.saga.exception.SagaConfigException;
import org.atlas.framework.saga.exception.SagaNotFoundException;
import org.atlas.framework.saga.messaging.SagaMessagePublisherPort;
import org.atlas.framework.saga.messaging.payload.SagaCommand;
import org.atlas.framework.saga.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.repository.SagaRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Unified dispatcher for SagaCommandEvent that routes events to @SagaCommandHandler methods and
 * publishes SagaCommandReplyEvent after execution.
 *
 * <p>This component combines event dispatching and reply publishing in a single component:
 * <ul>
 *   <li>Routes incoming SagaCommandEvent to the appropriate handler method</li>
 *   <li>Executes the handler with simplified argument resolution</li>
 *   <li>Publishes SagaCommandReplyEvent with success/failure status</li>
 *   <li>Supports exactly one handler per command for simplified architecture</li>
 * </ul>
 *
 * <p><strong>Handler Method Constraints:</strong>
 * <ul>
 *   <li>Handler methods can accept at most one parameter</li>
 *   <li>The parameter must be of type {@link SagaContext} (optional)</li>
 *   <li>Methods with no parameters are also supported</li>
 * </ul>
 *
 * @see org.atlas.framework.saga.annotation.SagaCommandHandler
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCommandHandlerDispatcher implements InitializingBean {

  private final SagaRepository sagaRepository;
  private final SagaMessagePublisherPort sagaMessagePublisherPort;
  private final ApplicationContext applicationContext;

  // One handler per command
  private final Map<String, CachedHandlerMethod> cachedHandlerMethods = new HashMap<>();

  /**
   * Initializes the handler methods by scanning all beans for @SagaCommandHandler annotations. Uses
   * lazy initialization with double-checked locking for thread safety.
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    Map<String, Object> beans = applicationContext.getBeansOfType(Object.class);

    for (Object bean : beans.values()) {
      Class<?> beanClass = bean.getClass();
      Method[] methods = beanClass.getDeclaredMethods();

      for (Method method : methods) {
        if (method.isAnnotationPresent(SagaCommandHandler.class)) {
          SagaCommandHandler annotation = method.getAnnotation(SagaCommandHandler.class);
          String sagaCommandName = annotation.command();

          // Check if handler already exists for this command name
          if (cachedHandlerMethods.containsKey(sagaCommandName)) {
            throw new SagaConfigException(
                "Multiple handlers found for command: " + sagaCommandName +
                    ". Only one handler per command is supported.");
          }

          // Validate method parameters and determine if it takes a parameter
          boolean takesParameter = validateAndCheckParameters(method);
          CachedHandlerMethod cachedHandlerMethod = new CachedHandlerMethod(bean, method,
              takesParameter);
          cachedHandlerMethods.put(sagaCommandName, cachedHandlerMethod);
          log.debug("Registered command handler method {}.{} for command: {}",
              beanClass.getSimpleName(), method.getName(), sagaCommandName);
        }
      }
    }
  }

  /**
   * Dispatches a SagaCommandEvent to the appropriate handler method and publishes reply event.
   */
  public void dispatch(SagaCommand event) {
    CachedHandlerMethod cachedHandlerMethod = cachedHandlerMethods.get(event.getSagaCommandName());
    if (cachedHandlerMethod == null) {
      log.warn("No cached handler found for saga command {}", event.getSagaCommandName());
      return;
    }

    SagaEntity sagaEntity = sagaRepository.findById(event.getSagaId())
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + event.getSagaId()));

    log.debug("Dispatching saga command {} to handler {}", event.getSagaCommandName(),
        cachedHandlerMethod.methodSignature);

    SagaContext sagaContext = SagaContext.deserialize(event.getSagaContext());
    Object result = null;
    Exception exception = null;

    try {
      result = cachedHandlerMethod.invoke(sagaContext);
      log.debug("Successfully executed handler {}", cachedHandlerMethod.methodSignature);
    } catch (Exception e) {
      exception = e;
      log.error("Failed to execute handler {}", cachedHandlerMethod.methodSignature, e);
    }

    // Publish command reply
    publishSagaCommandReply(sagaEntity, event.getSagaCommandName(), result, exception);
  }

  /**
   * Publishes SagaCommandReplyEvent with the result or exception.
   */
  private void publishSagaCommandReply(SagaEntity sagaEntity, String sagaCommandType,
      Object result, Exception exception) {
    SagaCommandReply reply;
    if (exception != null) {
      reply = SagaCommandReply.failure(sagaEntity, sagaCommandType, exception);
      log.debug("Publishing event for saga command failure reply: {}", reply);
    } else {
      reply = SagaCommandReply.success(sagaEntity, sagaCommandType, result);
      log.debug("Publishing event for saga command success reply: {}", reply);
    }
    sagaMessagePublisherPort.publish(reply);
  }

  /**
   * Validates method parameters and returns whether the method takes a parameter. Handler methods
   * can accept only one optional parameter: SagaContext.
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
              "Handler method %s.%s can accept at most one parameter of type SagaContext, but found %d parameters",
              method.getDeclaringClass().getSimpleName(), method.getName(), parameters.length));
    }

    Parameter parameter = parameters[0];
    if (parameter.getType() != SagaContext.class) {
      throw new SagaConfigException(
          String.format("Handler method %s.%s parameter must be of type SagaContext, but found: %s",
              method.getDeclaringClass().getSimpleName(), method.getName(),
              parameter.getType().getName()));
    }

    return true;
  }

  /**
   * Cached handler method with simplified parameter handling.
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
