package org.atlas.framework.saga.orchestrator;

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
}
