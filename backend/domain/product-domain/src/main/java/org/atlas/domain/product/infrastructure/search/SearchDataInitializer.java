package org.atlas.domain.product.infrastructure.search;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.util.PagingUtil;
import org.atlas.framework.util.SleepUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(SearchService.class)
@RequiredArgsConstructor
@Slf4j
public class SearchDataInitializer {

  private final SearchService searchService;
  private final ProductRepository productRepository;

  private static final int BATCH_SIZE = 100;

  @Async
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    if (searchService.initializeIndex(SearchIndex.PRODUCT)) {
      log.info("Product index created successfully, starting data synchronization...");
      synchronizeProductData();
    }
  }

  private void synchronizeProductData() {
    long startTime = System.currentTimeMillis();
    int synchronizedCount = 0;

    log.info(
        "Started synchronizing product data from database to Elasticsearch with batch size: {}",
        BATCH_SIZE);

    long totalProducts = productRepository.countAll();
    log.info("Total products to synchronize: {}", totalProducts);

    if (totalProducts == 0) {
      log.info("No products found in database, synchronization completed");
      return;
    }

    int totalBatches = PagingUtil.calcTotalPages(totalProducts, BATCH_SIZE);

    for (int batch = 0; batch < totalBatches; batch++) {
      long batchStartTime = System.currentTimeMillis();

      try {
        PagingRequest pagingRequest = PagingRequest.of(batch, BATCH_SIZE);
        FindProductCriteria criteria = new FindProductCriteria();

        PagingResult<ProductEntity> productPage = productRepository.findByCriteria(criteria,
            pagingRequest);
        List<ProductEntity> products = productPage.getData();

        if (products.isEmpty()) {
          log.warn("Empty batch encountered at page {}/{}", batch + 1, totalBatches);
          continue;
        }

        // Synchronize batch to Elasticsearch
        searchService.saveAll(products);
        synchronizedCount += products.size();

        long batchDuration = System.currentTimeMillis() - batchStartTime;
        log.info(
            "Successfully synchronized batch {}/{}. Current synchronized: {}/{}. Duration: {}ms",
            batch + 1, totalBatches, synchronizedCount, totalProducts, batchDuration);

        // Add small delay between batches to avoid overwhelming the system
        SleepUtil.sleep(100);
      } catch (Exception e) {
        log.error("Failed to synchronize batch {}/{} - Error: {}", batch + 1, totalBatches,
            e.getMessage(), e);

        // Continue with next batch instead of failing completely
        SleepUtil.sleep(1000);
      }
    }

    long totalDuration = System.currentTimeMillis() - startTime;

    log.info(
        "Product data synchronization completed. Total synchronized: {}/{}. Total duration: {}ms",
        synchronizedCount, totalProducts, totalDuration);
  }
}
