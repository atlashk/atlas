package org.atlas.domain.product.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.event.mapper.ProductEventMapper;
import org.atlas.domain.product.infrastructure.search.SearchService;
import org.atlas.framework.cache.ApplicationCache;
import org.atlas.framework.cache.CacheService;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.product.ProductEvent;
import org.atlas.framework.domain.event.handler.DomainEventHandler;
import org.atlas.framework.util.AsyncUtil;

@DomainEventHandler(type = DomainEventType.PRODUCT_UPDATED)
@RequiredArgsConstructor
@Slf4j
public class ProductUpdatedHandler {

  private final SearchService searchService;
  private final CacheService cacheService;

  public void handle(ProductEvent event) {
    Product product = ProductEventMapper.INSTANCE.toProduct(event);

    AsyncUtil.executeTasks(
        updateProductSearchDocument(product),
        evictProductCache(product)
    ).whenComplete((result, error) -> {
      if (error == null) {
        log.info("Completed handling product updated event: productId={}", product.getId());
      }
    });
  }

  private AsyncUtil.AsyncTask updateProductSearchDocument(Product product) {
    return new AsyncUtil.AsyncTask() {
      @Override
      public void run() {
        searchService.save(product);
      }

      @Override
      public void onSuccess() {
        log.info("Updated product search document: productId={}", product.getId());
      }

      @Override
      public void onError(Throwable e) {
        log.error("Failed to update product search document: productId={}, error={}",
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
