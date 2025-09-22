package org.atlas.infrastructure.lock.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Lock {

  String key();

  long waitTime();

  long leaseTime();

  TimeUnit timeUnit();

  boolean unlockOnCompletion() default true;
}
