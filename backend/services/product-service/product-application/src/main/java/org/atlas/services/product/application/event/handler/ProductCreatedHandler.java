package org.atlas.services.product.application.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.concurrent.AsyncUtil;
import org.atlas.libs.framework.domain.common.event.DomainEventType;
import org.atlas.libs.framework.domain.common.event.contract.product.ProductEvent;
import org.atlas.libs.framework.domain.common.event.handler.DomainEventHandler;
import org.atlas.services.product.application.event.mapper.ProductEventMapper;
import org.atlas.services.product.port.out.fulltextsearch.FullTextSearchService;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.springframework.beans.factory.ObjectProvider;

@DomainEventHandler(type = DomainEventType.PRODUCT_CREATED)
@RequiredArgsConstructor
@Slf4j
public class ProductCreatedHandler {

  private final ObjectProvider<FullTextSearchService> fullTextSearchServiceProvider;

  public void handle(ProductEvent event) {
    ProductEntity product = ProductEventMapper.INSTANCE.toProduct(event);

    AsyncUtil.executeTasks(
        createFullTextSearchDocument(product)
    ).whenComplete((result, error) -> {
      if (error == null) {
        log.info("Completed handling product created event: productId={}", product.getProductId());
      }
    });
  }

  private AsyncUtil.AsyncTask createFullTextSearchDocument(ProductEntity product) {
    return new AsyncUtil.AsyncTask() {
      @Override
      public void run() {
        FullTextSearchService fullTextSearchService = fullTextSearchServiceProvider.getIfAvailable();
        if (fullTextSearchService != null) {
          fullTextSearchService.save(product);
        }
      }

      @Override
      public void onSuccess() {
        log.info("Created full-text search document: productId={}", product.getProductId());
      }

      @Override
      public void onError(Throwable e) {
        log.error("Failed to create full-text search document: productId={}, error={}",
            product.getProductId(), e.getMessage(), e);
      }
    };
  }
}
