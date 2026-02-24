package org.atlas.libs.messaging.kafka.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaTopics {

  // Catalog service
  public static final String PRODUCT_EVENTS = "product_events";

  // Inventory service
  public static final String STOCK_EVENTS = "stock_events";

  // Order service
  public static final String ORDER_EVENTS = "order_events";
}
