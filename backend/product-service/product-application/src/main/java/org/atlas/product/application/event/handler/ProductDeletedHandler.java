package org.atlas.product.application.event.handler;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.product.application.event.mapper.ProductEventMapper;
import org.atlas.product.application.port.fulltextsearch.FullTextSearchService;
import org.atlas.product.application.service.ProductImageService;
import org.atlas.product.domain.entity.Product;
import org.atlas.common.framework.cache.ApplicationCache;
import org.atlas.common.framework.cache.CacheService;
import org.atlas.common.framework.concurrent.AsyncUtil;
import org.atlas.common.framework.domain.common.event.DomainEventType;
import org.atlas.common.framework.domain.common.event.contract.product.ProductEvent;
import org.atlas.common.framework.domain.common.event.handler.DomainEventHandler;
import org.springframework.beans.factory.ObjectProvider;

@DomainEventHandler(type = DomainEventType.PRODUCT_DELETED)
@RequiredArgsConstructor
@Slf4j
public class ProductDeletedHandler {

  private final ProductImageService productImageService;
  private final ObjectProvider<FullTextSearchService> fullTextSearchServiceProvider;
  private final CacheService cacheService;

  public void handle(ProductEvent event) {
    Product product = ProductEventMapper.INSTANCE.toProduct(event);

    AsyncUtil.executeTasks(
        deleteProductImage(product),
        deleteFullTextSearchDocument(product),
        evictProductCache(product)
    ).whenComplete((result, error) -> {
      if (error == null) {
        log.info("Completed handling product deleted event: productId={}", product.getId());
      }
    });
  }

  private AsyncUtil.AsyncTask deleteProductImage(Product product) {
    return new AsyncUtil.AsyncTask() {
      @Override
      public void run() {
        try {
          productImageService.deleteImage(product.getId());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void onSuccess() {
        log.info("Deleted product image: productId={}", product.getId());
      }

      @Override
      public void onError(Throwable e) {
        log.error("Failed to delete product image: productId={}, error={}",
            product.getId(), e.getMessage(), e);
      }
    };
  }

  private AsyncUtil.AsyncTask deleteFullTextSearchDocument(Product product) {
    return new AsyncUtil.AsyncTask() {
      @Override
      public void run() {
        FullTextSearchService fullTextSearchService = fullTextSearchServiceProvider.getIfAvailable();
        if (fullTextSearchService != null) {
          fullTextSearchService.deleteProduct(product.getId());
        }
      }

      @Override
      public void onSuccess() {
        log.info("Deleted full-text search document: productId={}", product.getId());
      }

      @Override
      public void onError(Throwable e) {
        log.error("Failed to delete full-text search document: productId={}, error={}",
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
