package org.atlas.infrastructure.messaging.rabbitmq.core;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RabbitmqConstant {

  public static final String USER_EVENTS_EXCHANGE = "user_events";
  public static final String PRODUCT_EVENTS_EXCHANGE = "product_events";
  public static final String ORDER_EVENTS_EXCHANGE = "order_events";
}
