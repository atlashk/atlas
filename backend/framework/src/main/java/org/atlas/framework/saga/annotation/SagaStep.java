package org.atlas.framework.saga.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SagaStep {

  String name();

  int order() default 0;

  String compensation() default "";

  boolean required() default true;

  long timeoutMs() default 30000; // 30 seconds default

  int maxRetries() default 0;

  long retryDelayMs() default 5000; // 5 seconds default

  String description() default "";
}
