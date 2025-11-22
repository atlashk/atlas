package org.atlas.domain.product.infrastructure.search;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.framework.hook.StartupHook;
import org.atlas.framework.measurement.StopWatch;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.util.PagingUtil;
import org.atlas.framework.util.SleepUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

@StartupHook
@ConditionalOnBean(SearchService.class)
@RequiredArgsConstructor
@Slf4j
public class SearchDataInitializer {

  private final SearchService searchService;
  private final ProductRepository productRepository;

  private static final int BATCH_SIZE = 100;

  public void handle() {
    synchronizeProductData();
  }

  private void synchronizeProductData() {
    // Create index if not exist
    boolean createdIndex = searchService.createIndex(SearchIndex.PRODUCT);
    if (!createdIndex) {
      log.info("Index of product has been created");
    }

    // Count documents to determine if synchronization is needed
    long documentCount = searchService.countDocuments(SearchIndex.PRODUCT);
    if (documentCount > 0) {
      log.info("Product data has been already synchronized");
      return;
    }

    StopWatch stopWatch = new StopWatch();
    int synchronizedCount = 0;

    long totalProducts = productRepository.countAll();
    log.info("Total products to synchronize: {}", totalProducts);

    if (totalProducts == 0) {
      log.info("No products found in database, synchronization completed");
      return;
    }

    log.info(
        "Started synchronizing product data from database to Elasticsearch with batch size: {}",
        BATCH_SIZE);
    stopWatch.start();

    int totalBatches = PagingUtil.calcTotalPages(totalProducts, BATCH_SIZE);

    for (int batch = 0; batch < totalBatches; batch++) {
      long batchStartTime = System.currentTimeMillis();

      try {
        PagingRequest pagingRequest = PagingRequest.of(batch, BATCH_SIZE);
        FindProductCriteria criteria = new FindProductCriteria();

        PagingResult<Product> productPage = productRepository.findByCriteria(criteria,
            pagingRequest);
        List<Product> products = productPage.getData();

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

    stopWatch.stop();
    log.info(
        "Product data synchronization completed. Total synchronized: {}/{}. Total duration: {}ms",
        synchronizedCount, totalProducts, stopWatch.getElapsedTimeMs());
  }
}
