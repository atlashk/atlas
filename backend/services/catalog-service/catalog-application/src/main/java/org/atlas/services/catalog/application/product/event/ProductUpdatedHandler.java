package org.atlas.services.catalog.application.product.event;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cache.ApplicationCache;
import org.atlas.libs.framework.cache.CacheService;
import org.atlas.libs.framework.concurrent.AsyncUtil;
import org.atlas.libs.framework.concurrent.AsyncUtil.AsyncTask;
import org.atlas.libs.framework.domain.common.event.DomainEventType;
import org.atlas.libs.framework.domain.common.event.contract.product.ProductCreatedEvent;
import org.atlas.libs.framework.domain.common.event.handler.DomainEventHandler;
import org.atlas.services.catalog.application.product.mapper.ProductEventMapper;
import org.atlas.services.catalog.domain.entity.ProductEntity;

@DomainEventHandler(type = DomainEventType.PRODUCT_UPDATED)
@RequiredArgsConstructor
@Slf4j
public class ProductUpdatedHandler {

  private final CacheService cacheService;

  public void handle(ProductCreatedEvent event) {
    ProductEntity product = ProductEventMapper.INSTANCE.toProduct(event);

    List<AsyncTask> tasks = new ArrayList<>();
    tasks.add(evictProductCache(product));

    AsyncUtil.executeTasks(tasks)
        .whenComplete((result, error) -> {
          if (error == null) {
            log.info("Completed handling product updated event: id={}", product.getId());
          }
        });
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
