package org.atlas.libs.framework.appstack;

import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppStackService {

  private final ApplicationConfigService applicationConfigService;

  public String getServiceName(String serviceKey) {
    return applicationConfigService.getConfig("appStack." + serviceKey);
  }
}
