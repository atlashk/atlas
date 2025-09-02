package org.atlas.infrastructure.kv.redis;

import org.atlas.framework.kv.KvConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RedisKvConfig implements KvConfig {

  @Value("${REDIS_PRODUCT_STORE:product}")
  private String productStoreName;

  @Value("${REDIS_EVENT_STORE:event}")
  private String eventStoreName;

  @Override
  public String getProductStoreName() {
    return productStoreName;
  }

  @Override
  public String getEventStoreName() {
    return eventStoreName;
  }
}
