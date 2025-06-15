package org.atlas.domain.product.port.messaging;

import org.atlas.framework.domain.event.contract.product.ProductCreatedEvent;
import org.atlas.framework.domain.event.contract.product.ProductDeletedEvent;
import org.atlas.framework.domain.event.contract.product.ProductUpdatedEvent;
import org.atlas.framework.domain.event.contract.product.ReserveQuantityFailedEvent;
import org.atlas.framework.domain.event.contract.product.ReserveQuantitySucceededEvent;

public interface ProductMessagePublisherPort {

  void publish(ProductCreatedEvent event);

  void publish(ProductUpdatedEvent event);

  void publish(ProductDeletedEvent event);

  void publish(ReserveQuantitySucceededEvent event);

  void publish(ReserveQuantityFailedEvent event);
}
