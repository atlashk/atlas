package org.atlas.services.product.application.event.handler;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cache.ApplicationCache;
import org.atlas.libs.framework.cache.CacheService;
import org.atlas.libs.framework.concurrent.AsyncUtil;
import org.atlas.libs.framework.concurrent.AsyncUtil.AsyncTask;
import org.atlas.libs.framework.domain.common.event.DomainEventType;
import org.atlas.libs.framework.domain.common.event.contract.product.ProductEvent;
import org.atlas.libs.framework.domain.common.event.handler.DomainEventHandler;
import org.atlas.services.product.application.event.mapper.ProductEventMapper;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.port.in.front.service.ProductImageService;
import org.atlas.services.product.port.out.fulltextsearch.FullTextSearchService;
import org.springframework.beans.factory.ObjectProvider;

@DomainEventHandler(type = DomainEventType.PRODUCT_DELETED)
@RequiredArgsConstructor
@Slf4j
public class ProductDeletedHandler {

  private final ProductImageService productImageService;
  private final ObjectProvider<FullTextSearchService> fullTextSearchServiceProvider;
  private final CacheService cacheService;

  public void handle(ProductEvent event) {
    ProductEntity product = ProductEventMapper.INSTANCE.toProduct(event);

    List<AsyncTask> tasks = new ArrayList<>();
    tasks.add(deleteProductImage(product));
    tasks.add(evictProductCache(product));
    FullTextSearchService fullTextSearchService = fullTextSearchServiceProvider.getIfAvailable();
    if (fullTextSearchService != null) {
      tasks.add(deleteFullTextSearchDocument(product));
    }

    AsyncUtil.executeTasks(tasks)
        .whenComplete((result, error) -> {
          if (error == null) {
            log.info("Completed handling product deleted event: productId={}", product.getId());
          }
        });
  }

  private AsyncUtil.AsyncTask deleteProductImage(ProductEntity product) {
    return new AsyncUtil.AsyncTask() {
      @Override
      public void run() {
        productImageService.deleteImage(product.getId());
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

  private AsyncUtil.AsyncTask deleteFullTextSearchDocument(ProductEntity product) {
    return new AsyncUtil.AsyncTask() {
      @Override
      public void run() {
        FullTextSearchService fullTextSearchService = fullTextSearchServiceProvider.getObject();
        fullTextSearchService.deleteProduct(product.getId());
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

  private AsyncUtil.AsyncTask evictProductCache(ProductEntity product) {
    return new AsyncUtil.AsyncTask() {
      @Override
      public void run() {
        cacheService.evict(ApplicationCache.PRODUCT, product.getId());
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
