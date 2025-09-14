package org.atlas.framework.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Application {

  public static final String SYSTEM = "system";
  public static final String USER_SERVICE = "user-service";
  public static final String PRODUCT_SERVICE = "product-service";
  public static final String ORDER_SERVICE = "order-service";
  public static final String NOTIFICATION_SERVICE = "notification-service";
}
