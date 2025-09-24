package org.atlas.infrastructure.messaging.external.sns.core.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SnsConstant {

  // SNS Topics ARN
  public static final String SNS_TOPIC_ARN_USER_EVENT = "SNS_TOPIC_ARN_USER_EVENT";
  public static final String SNS_TOPIC_ARN_PRODUCT_EVENT = "SNS_TOPIC_ARN_PRODUCT_EVENT";
  public static final String SNS_TOPIC_ARN_ORDER_EVENT = "SNS_TOPIC_ARN_ORDER_EVENT";

  // SQS Queues URL
  // Product queues
  public static final String QUEUE_PRODUCT_SVC_ORDER_EVENT = "product_svc_order_event";
  // Order queues
  public static final String QUEUE_ORDER_SVC_ORDER_EVENT = "order_svc_order_event";
  // Notification queues
  public static final String QUEUE_PAYMENT_SVC_ORDER_EVENT = "payment_svc_order_event";
}
