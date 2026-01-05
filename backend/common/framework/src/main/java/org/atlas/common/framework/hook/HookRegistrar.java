package org.atlas.common.framework.hook;

import java.util.Comparator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.common.framework.measurement.StopWatch;
import org.atlas.common.framework.util.ReflectionUtil;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HookRegistrar implements InitializingBean, DisposableBean {

  private final ApplicationContext applicationContext;

  private static final int DEFAULT_ORDER = 0;

  @Override
  public void afterPropertiesSet() {
    Map<String, Object> startupHooks = applicationContext.getBeansWithAnnotation(StartupHook.class);
    invokeHooks(startupHooks, HookType.STARTUP);
  }

  @Override
  public void destroy() {
    Map<String, Object> shutdownHooks = applicationContext.getBeansWithAnnotation(
        ShutdownHook.class);
    invokeHooks(shutdownHooks, HookType.SHUTDOWN);
  }

  private void invokeHooks(Map<String, Object> hooks, HookType hookType) {
    if (hooks == null || hooks.isEmpty()) {
      log.info("No hooks found, skip them");
      return;
    }

    var orderedHooks = hooks.entrySet()
        .stream()
        .sorted(Comparator
            .comparingInt((Map.Entry<String, Object> e) -> getOrder(e.getValue(), hookType))
            .thenComparing(Map.Entry::getKey)) // tie-breaker for deterministic order
        .toList();

    log.info("Invoking {} hooks (sorted by order asc)", orderedHooks.size());

    for (var entry : orderedHooks) {
      String hookBeanName = entry.getKey();
      Object hookBean = entry.getValue();

      log.info("Started hook {}", hookBeanName);
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      try {
        ReflectionUtil.invokeMethod(hookBean, "handle");
      } catch (Exception e) {
        log.error("Failed to execute hook {}: error={}", hookBeanName, e.getCause().getMessage(),
            e.getCause());
      } finally {
        stopWatch.stop();
        log.info("Finished hook {} in {} ms", hookBeanName, stopWatch.getElapsedTimeMs());
      }
    }
  }

  private int getOrder(Object bean, HookType hookType) {
    Class<?> targetClass = AopUtils.getTargetClass(bean); // important if bean is proxied

    return switch (hookType) {
      case STARTUP -> {
        StartupHook ann = AnnotationUtils.findAnnotation(targetClass, StartupHook.class);
        yield (ann == null) ? DEFAULT_ORDER : ann.order();
      }
      case SHUTDOWN -> {
        ShutdownHook ann = AnnotationUtils.findAnnotation(targetClass, ShutdownHook.class);
        yield (ann == null) ? DEFAULT_ORDER : ann.order();
      }
    };
  }

  private enum HookType {STARTUP, SHUTDOWN}
}
