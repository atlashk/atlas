package org.atlas.framework.domain.event.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.atlas.framework.domain.event.DomainEventType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DomainEventHandler {

  DomainEventType type();
}
