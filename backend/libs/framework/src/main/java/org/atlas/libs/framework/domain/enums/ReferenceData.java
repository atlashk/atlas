package org.atlas.libs.framework.domain.enums;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark an enum as reference data.
 * The enum will be automatically registered and available via the reference data API.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReferenceData {

  /**
   * The key used to retrieve this reference data via API.
   * If not specified, the enum class simple name in UPPER_SNAKE_CASE will be used.
   * Example: OrderStatus -> ORDER_STATUS
   */
  String value() default "";
}

