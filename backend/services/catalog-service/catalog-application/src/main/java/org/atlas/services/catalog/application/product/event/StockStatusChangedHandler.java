package org.atlas.services.catalog.application.product.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.event.DomainEventType;
import org.atlas.libs.framework.domain.event.contract.inventory.StockStatusChangedEvent;
import org.atlas.libs.framework.domain.event.handler.DomainEventHandler;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.domain.error.CatalogDomainError;
import org.atlas.services.catalog.port.out.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

@DomainEventHandler(type = DomainEventType.STOCK_STATUS_CHANGED)
@RequiredArgsConstructor
@Slf4j
public class StockStatusChangedHandler {

  private final ProductRepository productRepository;

  @Transactional
  public void handle(StockStatusChangedEvent event) {
    ProductEntity product = productRepository.findById(event.getProductId())
        .orElseThrow(() -> new DomainException(CatalogDomainError.PRODUCT_NOT_FOUND));

    boolean updated = false;
    if (event.getStockStatus() == StockStatusChangedEvent.StockStatus.BACK_IN_STOCK) {
      product.setInStock(true);
      updated = true;
    }

    if (event.getStockStatus() == StockStatusChangedEvent.StockStatus.OUT_OF_STOCK) {
      product.setInStock(false);
      updated = true;
    }

    if (updated) {
      productRepository.update(product);
    }
  }
}
