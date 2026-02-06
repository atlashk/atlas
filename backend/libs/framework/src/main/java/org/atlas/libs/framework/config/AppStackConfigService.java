package org.atlas.libs.framework.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppStackConfigService {

  private final ApplicationConfigService applicationConfigService;

  public String getServiceName(String serviceKey) {
    return applicationConfigService.getConfig("appStack." + serviceKey);
  }
}
