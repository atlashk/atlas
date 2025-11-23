package org.atlas.domain.product.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.event.mapper.ProductEventMapper;
import org.atlas.domain.product.infrastructure.search.SearchService;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.product.ProductEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;

@DomainEventHandler(type = DomainEventType.PRODUCT_CREATED)
@RequiredArgsConstructor
@Slf4j
public class ProductCreatedHandler {

  private final SearchService searchService;

  public void handle(ProductEvent event) {
    Product product = ProductEventMapper.INSTANCE.toProduct(event);

    try {
      searchService.save(product);
      log.info("Created search document: productId={}", product.getId());
    } catch (Exception e) {
      log.error("Failed to create search document: productId={}", product.getId(), e);
    }

    log.info("Completed handling product created event: productId={}", product.getId());
  }
}
