package org.atlas.infrastructure.messaging.rabbitmq.core.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RabbitmqConstant {

  // Exchanges
  public static final String EXCHANGE_USER_EVENT = "user_event";
  public static final String EXCHANGE_PRODUCT_EVENT = "product_event";
  public static final String EXCHANGE_ORDER_EVENT = "order_event";

  // Queues
  // Product service
  public static final String QUEUE_PRODUCT_SVC_ORDER_EVENT = "product_svc_order_event";
  // Order service
  public static final String QUEUE_ORDER_SVC_ORDER_EVENT = "order_svc_order_event";
  // Payment service
  public static final String QUEUE_PAYMENT_SVC_ORDER_EVENT = "payment_svc_order_event";
}
