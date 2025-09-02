package org.atlas.infrastructure.messaging.kafka.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaConstant {

  public static final String USER_EVENT_TOPIC = "user_event";
  public static final String PRODUCT_EVENT_TOPIC = "product_event";
  public static final String ORDER_EVENT_TOPIC = "order_event";
}
