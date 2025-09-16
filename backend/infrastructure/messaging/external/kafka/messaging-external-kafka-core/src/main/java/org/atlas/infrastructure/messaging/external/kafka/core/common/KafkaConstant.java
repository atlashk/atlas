package org.atlas.infrastructure.messaging.external.kafka.core.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaConstant {

  // Topics
  public static final String TOPIC_USER_EVENT = "user_event";
  public static final String TOPIC_PRODUCT_EVENT = "product_event";
  public static final String TOPIC_ORDER_EVENT = "order_event";
}
