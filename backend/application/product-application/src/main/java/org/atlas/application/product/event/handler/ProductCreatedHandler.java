package org.atlas.application.product.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.application.product.event.mapper.ProductEventMapper;
import org.atlas.application.product.port.fulltextsearch.FullTextSearchService;
import org.atlas.domain.product.entity.Product;
import org.atlas.framework.concurrent.AsyncUtil;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.product.ProductEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.springframework.beans.factory.ObjectProvider;

@DomainEventHandler(type = DomainEventType.PRODUCT_CREATED)
@RequiredArgsConstructor
@Slf4j
public class ProductCreatedHandler {

  private final ObjectProvider<FullTextSearchService> fullTextSearchServiceProvider;

  public void handle(ProductEvent event) {
    Product product = ProductEventMapper.INSTANCE.toProduct(event);

    AsyncUtil.executeTasks(
        createFullTextSearchDocument(product)
    ).whenComplete((result, error) -> {
      if (error == null) {
        log.info("Completed handling product created event: productId={}", product.getId());
      }
    });
  }

  private AsyncUtil.AsyncTask createFullTextSearchDocument(Product product) {
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
        log.info("Created full-text search document: productId={}", product.getId());
      }

      @Override
      public void onError(Throwable e) {
        log.error("Failed to create full-text search document: productId={}, error={}",
            product.getId(), e.getMessage(), e);
      }
    };
  }
}
