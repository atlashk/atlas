package org.atlas.services.product.application.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cache.ApplicationCache;
import org.atlas.libs.framework.cache.CacheService;
import org.atlas.libs.framework.concurrent.AsyncUtil;
import org.atlas.libs.framework.domain.common.event.DomainEventType;
import org.atlas.libs.framework.domain.common.event.contract.product.ProductEvent;
import org.atlas.libs.framework.domain.common.event.handler.DomainEventHandler;
import org.atlas.services.product.application.event.mapper.ProductEventMapper;
import org.atlas.services.product.application.port.fulltextsearch.FullTextSearchService;
import org.atlas.services.product.domain.entity.Product;

@DomainEventHandler(type = DomainEventType.PRODUCT_UPDATED)
@RequiredArgsConstructor
@Slf4j
public class ProductUpdatedHandler {

  private final FullTextSearchService fullTextSearchService;
  private final CacheService cacheService;

  public void handle(ProductEvent event) {
    Product product = ProductEventMapper.INSTANCE.toProduct(event);

    AsyncUtil.executeTasks(
        updateFullTextSearchDocument(product),
        evictProductCache(product)
    ).whenComplete((result, error) -> {
      if (error == null) {
        log.info("Completed handling product updated event: productId={}", product.getId());
      }
    });
  }

  private AsyncUtil.AsyncTask updateFullTextSearchDocument(Product product) {
    return new AsyncUtil.AsyncTask() {
      @Override
      public void run() {
        fullTextSearchService.save(product);
      }

      @Override
      public void onSuccess() {
        log.info("Updated full-text search document: productId={}", product.getId());
      }

      @Override
      public void onError(Throwable e) {
        log.error("Failed to update full-text search document: productId={}, error={}",
            product.getId(), e.getMessage(), e);
      }
    };
  }

  private AsyncUtil.AsyncTask evictProductCache(Product product) {
    return new AsyncUtil.AsyncTask() {
      @Override
      public void run() {
        cacheService.evict(ApplicationCache.PRODUCT, String.valueOf(product.getId()));
      }

      @Override
      public void onSuccess() {
        log.info("Evicted product cache: productId={}", product.getId());
      }

      @Override
      public void onError(Throwable e) {
        log.error("Failed to evict product cache: productId={}, error={}",
            product.getId(), e.getMessage(), e);
      }
    };
  }
}
