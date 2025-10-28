package org.atlas.framework.saga.core.command;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.core.annotation.SagaCommandHandler;
import org.atlas.framework.saga.core.exception.SagaConfigException;
import org.atlas.framework.saga.core.messaging.SagaMessagePublisher;
import org.atlas.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.framework.saga.core.messaging.payload.SagaCommandReply;
import org.atlas.framework.util.ErrorUtil;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
 *   <li>The parameter must be of type {@link SagaCommand} (optional)</li>
 *   <li>Methods with no parameters are also supported</li>
 * </ul>
 *
 * @see SagaCommandHandler
 */
@Component
@ConditionalOnBean(SagaMessagePublisher.class)
@RequiredArgsConstructor
@Slf4j
public class SagaCommandHandlerDispatcher {

  private final SagaMessagePublisher sagaMessagePublisher;
  private final ApplicationContext applicationContext;

  // One handler per command - using ConcurrentHashMap for thread safety
  private final Map<String, CachedHandlerMethod> cachedHandlerMethods = new ConcurrentHashMap<>();
  private volatile boolean initialized = false;

  /**
   * Initializes the handler methods by scanning all beans for @SagaCommandHandler annotations. Uses
   * lazy initialization with double-checked locking for thread safety.
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
   * Dispatches a SagaCommandEvent to the appropriate handler method and publishes reply.
   */
  public void dispatch(SagaCommand sagaCommand) {
    // Ensure handlers are initialized before dispatching
    initializeHandlers();

    CachedHandlerMethod cachedHandlerMethod = cachedHandlerMethods.get(
        sagaCommand.getSagaCommandName());
    if (cachedHandlerMethod == null) {
      log.warn("No cached handler found for saga command {}", sagaCommand.getSagaCommandName());
      return;
    }

    SagaCommandResult sagaCommandResult;
    try {
      log.debug("Dispatching saga command {} to handler {}", sagaCommand.getSagaCommandName(),
          cachedHandlerMethod.methodSignature);
      sagaCommandResult = (SagaCommandResult) cachedHandlerMethod.invoke(sagaCommand);
      if (sagaCommandResult.isSuccess()) {
        log.info("Successfully executed saga command handler {}",
            cachedHandlerMethod.methodSignature);
      } else {
        log.error("Failed to execute saga command handler {}: {}",
            cachedHandlerMethod.methodSignature, sagaCommandResult.getError());
      }
    } catch (Exception e) {
      Throwable cause = ErrorUtil.getRootCause(e);
      sagaCommandResult = SagaCommandResult.failure(
          ErrorUtil.sanitizeErrorMessage(cause.getMessage()));
      log.error("Failed to execute saga command handler {}: {}",
          cachedHandlerMethod.methodSignature, sagaCommandResult.getError(), cause);
    }

    // Publish command reply
    SagaCommandReply reply = SagaCommandReply.builder()
        .sagaId(sagaCommand.getSagaId())
        .sagaName(sagaCommand.getSagaName())
        .sagaCommandName(sagaCommand.getSagaCommandName())
        .sagaCommandResult(sagaCommandResult)
        .build();
    sagaMessagePublisher.publish(reply);
  }

  /**
   * Validates method parameters and returns whether the method takes a parameter. Handler methods
   * can accept only one optional parameter: {@link SagaCommand}.
   */
  private boolean validateAndCheckParameters(Method method) {
    Parameter[] parameters = method.getParameters();

    // No parameters
    if (parameters.length == 0) {
      return false;
    }

    // Validate that there's only one parameter and it's SagaCommand
    if (parameters.length > 1) {
      throw new SagaConfigException(
          String.format(
              "Handler method %s.%s can accept at most one parameter of type SagaCommand, but found %d parameters",
              method.getDeclaringClass().getSimpleName(), method.getName(), parameters.length));
    }

    Parameter parameter = parameters[0];
    if (parameter.getType() != SagaCommand.class) {
      throw new SagaConfigException(
          String.format("Handler method %s.%s parameter must be of type SagaCommand, but found: %s",
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

    public Object invoke(SagaCommand sagaCommand) throws Exception {
      if (takesParameter) {
        return method.invoke(bean, sagaCommand);
      } else {
        return method.invoke(bean);
      }
    }
  }
}
