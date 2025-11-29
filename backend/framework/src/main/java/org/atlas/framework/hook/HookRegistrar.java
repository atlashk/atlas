package org.atlas.framework.hook;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.measurement.StopWatch;
import org.atlas.framework.collection.MapUtil;
import org.atlas.framework.util.ReflectionUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HookRegistrar implements InitializingBean, DisposableBean {

  private final ApplicationContext applicationContext;

  @Override
  public void afterPropertiesSet() throws Exception {
    Map<String, Object> startupHooks = applicationContext.getBeansWithAnnotation(StartupHook.class);
    invokeHooks(startupHooks);
  }

  @Override
  public void destroy() throws Exception {
    Map<String, Object> shutdownHooks = applicationContext.getBeansWithAnnotation(
        ShutdownHook.class);
    invokeHooks(shutdownHooks);
  }

  private void invokeHooks(Map<String, Object> hooks) {
    if (MapUtil.isEmpty(hooks)) {
      log.info("No hooks found, skip them");
    }

    log.info("Invoking {} hooks", hooks.size());
    hooks.forEach((hookBeanName, hookBean) -> {
      log.info("Started hook {}", hookBeanName);
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      ReflectionUtil.invokeMethod(hookBean, "handle");
      stopWatch.stop();
      log.info("Finished hook {} in {} ms", hookBeanName, stopWatch.getElapsedTimeMs());
    });
  }
}
