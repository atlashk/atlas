package org.atlas.services.product.application.hook;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.hook.StartupHook;
import org.atlas.libs.framework.measurement.StopWatch;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.PagingUtil;
import org.atlas.libs.framework.util.SleepUtil;
import org.atlas.services.product.port.out.fulltextsearch.FullTextSearchService;
import org.atlas.services.product.port.out.fulltextsearch.SearchIndex;
import org.atlas.services.product.port.out.repository.ProductRepository;
import org.atlas.services.product.port.out.repository.criteria.FindProductCriteria;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

@StartupHook
@ConditionalOnBean(FullTextSearchService.class)
@RequiredArgsConstructor
@Slf4j
public class ProductDataInitializer {

  private final ObjectProvider<FullTextSearchService> fullTextSearchServiceProvider;
  private final ProductRepository productRepository;

  private static final int BATCH_SIZE = 100;

  public void handle() {
    FullTextSearchService fullTextSearchService = fullTextSearchServiceProvider.getIfAvailable();
    if (fullTextSearchService != null) {
      synchronizeFullTextSearchData();
    }
  }

  private void synchronizeFullTextSearchData() {
    FullTextSearchService fullTextSearchService = fullTextSearchServiceProvider.getObject();

    // Create index if not exist
    boolean createdIndex = fullTextSearchService.createIndex(SearchIndex.PRODUCT);
    if (!createdIndex) {
      log.info("Index of product has been created");
    }

    // Count documents to determine if synchronization is needed
    long documentCount = fullTextSearchService.countDocuments(SearchIndex.PRODUCT);
    if (documentCount > 0) {
      log.info("Product data has been already synchronized");
      return;
    }

    int synchronizedCount = 0;

    long totalProducts = productRepository.countAll();
    log.info("Total products to synchronize: {}", totalProducts);

    if (totalProducts == 0) {
      log.info("No products found in database, synchronization completed");
      return;
    }

    log.info(
        "Started synchronizing productPayload data from database to Elasticsearch with batch size: {}",
        BATCH_SIZE);
    StopWatch stopWatch = new StopWatch();
    StopWatch batchStopWatch = new StopWatch();
    stopWatch.start();

    int totalBatches = PagingUtil.calcTotalPages(totalProducts, BATCH_SIZE);

    for (int batch = 0; batch < totalBatches; batch++) {
      batchStopWatch.start();

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
        fullTextSearchService.saveAll(products);
        synchronizedCount += products.size();

        batchStopWatch.stop();
        log.info(
            "Successfully synchronized batch {}/{}. Current synchronized: {}/{}. Duration: {}ms",
            batch + 1, totalBatches, synchronizedCount, totalProducts,
            batchStopWatch.getElapsedTimeMs());

        // Add small delay between batches to avoid overwhelming the system
        SleepUtil.sleep(100);
      } catch (Exception e) {
        batchStopWatch.stop();
        log.error("Failed to synchronize batch {}/{} - Error: {}", batch + 1, totalBatches,
            e.getMessage(), e);

        // Continue with next batch instead of failing completely
        SleepUtil.sleep(1000);
      } finally {
        batchStopWatch.reset();
      }
    }

    stopWatch.stop();
    log.info(
        "Product data synchronization completed. Total synchronized: {}/{}. Total duration: {}ms",
        synchronizedCount, totalProducts, stopWatch.getElapsedTimeMs());
  }
}
