package org.atlas.services.catalog.application.product.initializer;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.PagingUtil;
import org.atlas.libs.framework.util.SleepUtil;
import org.atlas.libs.framework.util.StopWatch;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.port.out.ai.rag.service.ProductVectorStoreService;
import org.atlas.services.catalog.port.out.repository.ProductRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductVectorStoreInitializer {

  private final ProductVectorStoreService productVectorStoreService;
  private final ProductRepository productRepository;

  private static final int BATCH_SIZE = 100;

  @EventListener(ApplicationReadyEvent.class)
  public void initialize(ApplicationReadyEvent event) {
    try {
      int initializedCount = 0;

      long totalProducts = productRepository.countAll();
      log.info("Total products to initialize: {}", totalProducts);

      if (totalProducts == 0) {
        log.info("No products found in database, initialization completed");
        return;
      }

      StopWatch stopWatch = new StopWatch();
      StopWatch batchStopWatch = new StopWatch();
      stopWatch.start();

      int totalBatches = PagingUtil.calcTotalPages(totalProducts, BATCH_SIZE);

      for (int batch = 0; batch < totalBatches; batch++) {
        batchStopWatch.start();

        try {
          PagingRequest pagingRequest = PagingRequest.of(batch, BATCH_SIZE);
          ProductRepository.FindProductCriteria criteria = new ProductRepository.FindProductCriteria();

          PagingResult<ProductEntity> productPage = productRepository.findByCriteria(criteria,
              pagingRequest);
          List<ProductEntity> products = productPage.getData();

          if (products.isEmpty()) {
            log.warn("Empty batch encountered at page {}/{}", batch + 1, totalBatches);
            continue;
          }

          productVectorStoreService.addDocuments(products);
          initializedCount += products.size();

          batchStopWatch.stop();
          log.info(
              "Successfully processed batch {}/{}. Current initialized: {}/{}. Duration: {}ms",
              batch + 1, totalBatches, initializedCount, totalProducts,
              batchStopWatch.getElapsedTimeMs());

          // Add small delay between batches to avoid overwhelming the system
          SleepUtil.sleep(100);
        } catch (Exception e) {
          batchStopWatch.stop();
          log.error("Failed to process batch {}/{} - Error: {}", batch + 1, totalBatches,
              e.getMessage(), e);

          // Continue with next batch instead of failing completely
          SleepUtil.sleep(1000);
        } finally {
          batchStopWatch.reset();
        }
      }

      stopWatch.stop();
      log.info(
          "Product vector store initialization completed. Total initialized: {}/{}. Total duration: {}ms",
          initializedCount, totalProducts, stopWatch.getElapsedTimeMs());
    } catch (Exception e) {
      // Fail-fast
      log.error("Failed to initialize product vector store", e);
      SpringApplication.exit(event.getApplicationContext());
    }
  }
}
