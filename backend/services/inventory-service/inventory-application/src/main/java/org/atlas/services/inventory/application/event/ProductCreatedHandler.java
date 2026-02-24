package org.atlas.services.inventory.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.event.DomainEventType;
import org.atlas.libs.framework.domain.event.contract.catalog.ProductCreatedEvent;
import org.atlas.libs.framework.domain.event.handler.DomainEventHandler;
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.inventory.port.out.repository.StockRepository;
import org.springframework.transaction.annotation.Transactional;

@DomainEventHandler(type = DomainEventType.PRODUCT_CREATED)
@RequiredArgsConstructor
@Slf4j
public class ProductCreatedHandler {
  
  private final StockRepository stockRepository;
  
  @Transactional
  public void handle(ProductCreatedEvent event) {
    StockEntity stock = new StockEntity();
    stock.setProductId(event.getProductId());
    stock.setAvailableQuantity(event.getInitialQuantity());
    stock.setReservedQuantity(0);
    stockRepository.insert(stock);
  }
}
