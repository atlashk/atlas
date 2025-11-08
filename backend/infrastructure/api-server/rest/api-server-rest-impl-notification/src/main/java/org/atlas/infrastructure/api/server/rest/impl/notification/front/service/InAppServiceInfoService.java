package org.atlas.infrastructure.api.server.rest.impl.notification.front.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.notification.inapp.InAppService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InAppServiceInfoService {

  private final ApplicationContext applicationContext;

  public String getCurrentInAppServiceType() {
    // Check which InAppService implementation is available in the application context
    String[] beanNames = applicationContext.getBeanNamesForType(InAppService.class);
    
    if (beanNames.length == 0) {
      log.warn("No InAppService implementation found in application context");
      return "unknown";
    }
    
    if (beanNames.length > 1) {
      log.warn("Multiple InAppService implementations found: {}. Using the first one.", String.join(", ", beanNames));
    }
    
    String beanName = beanNames[0];
    log.debug("Found InAppService implementation: {}", beanName);
    
    // Return just the prefix by removing "InAppService" from the bean name
    String serviceType = beanName.replace("InAppService", "");
    log.debug("Extracted service type: {} from bean name: {}", serviceType, beanName);
    return serviceType;
  }
}