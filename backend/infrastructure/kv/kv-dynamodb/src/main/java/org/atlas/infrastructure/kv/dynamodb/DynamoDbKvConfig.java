package org.atlas.infrastructure.kv.dynamodb;

import org.atlas.framework.kv.KvConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DynamoDbKvConfig implements KvConfig {

  @Value("${DYNAMODB_PRODUCT_STORE:product}")
  private String productStoreName;

  @Value("${DYNAMODB_EVENT_STORE:event}")
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
