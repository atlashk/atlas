package org.atlas.framework.saga.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SagaCompensation {

  String name();

  int maxRetries() default 3;

  long retryDelayMs() default 1000;

  String description() default "";
}
