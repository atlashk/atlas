package org.atlas.framework.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum Application {

  SYSTEM("system"),
  USER_SERVICE("user-service"),
  PRODUCT_SERVICE("product-service"),
  ORDER_SERVICE("order-service"),
  NOTIFICATION_SERVICE("notification-service"),
  ;

  private final String value;
}
