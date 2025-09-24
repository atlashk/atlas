package org.atlas.infrastructure.kv.redis;

import org.atlas.framework.kv.KvConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RedisKvConfig implements KvConfig {

  @Value("${REDIS_PRODUCT_STORE:product}")
  private String productStoreName;

  @Value("${REDIS_CART_STORE:cart}")
  private String cartStoreName;

  @Override
  public String getProductStoreName() {
    return productStoreName;
  }

  @Override
  public String getCartStoreName() {
    return cartStoreName;
  }
}
