package org.atlas.services.catalog.application.product.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.storage.StorageService;
import org.atlas.libs.framework.util.PagingUtil;
import org.atlas.libs.framework.util.SleepUtil;
import org.atlas.libs.framework.util.StopWatch;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.port.in.product.service.ProductInitializerService;
import org.atlas.services.catalog.port.out.ai.chatbot.service.ProductVectorStoreService;
import org.atlas.services.catalog.port.out.repository.ProductRepository;
import org.atlas.services.catalog.port.out.search.ProductSearchService;
import org.atlas.services.catalog.port.out.storage.ProductStorageConstant;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductInitializerServiceImpl implements ProductInitializerService {

  private final StorageService storageService;
  private final ObjectProvider<ProductSearchService> searchServiceProvider;
  private final ObjectProvider<ProductVectorStoreService> vectorStoreServiceProvider;
  private final ProductRepository productRepository;

  private static final int BATCH_SIZE = 100;

  @Async
  @Override
  public void initializeImageBucket() throws Exception {
    storageService.createBucket(ProductStorageConstant.PRODUCT_IMAGE_BUCKET);
    log.info("The bucket of product image has been created.");
  }

  @Async
  @Override
  public void initializeSearchData() throws Exception {
    ProductSearchService productSearchService = searchServiceProvider.getIfAvailable();
    if (productSearchService == null) {
      log.warn("ProductSearchService is not available, skipping search data initialization.");
      return;
    }

    // Create index if not exist
    boolean createdIndex = productSearchService.createIndex();
    if (!createdIndex) {
      log.info("Index of product has been created");
    }

    // Count documents to determine if initialization is needed
    long documentCount = productSearchService.countDocuments();
    if (documentCount > 0) {
      log.info("Product data has been already initialized");
      return;
    }

    long totalProducts = productRepository.countAll();
    log.info("Total products to initialize: {}", totalProducts);

    if (totalProducts == 0) {
      log.info("No products found in database, initialization completed");
      return;
    }

    int initializedCount = 0;
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

        productSearchService.saveAll(products);
        initializedCount += products.size();

        batchStopWatch.stop();
        log.info(
            "Successfully processed batch {}/{}. Current initialized: {}/{}. Duration: {}ms",
            batch + 1, totalBatches, initializedCount, totalProducts,
            batchStopWatch.getElapsedTimeMs());

        SleepUtil.sleep(100);
      } catch (Exception e) {
        batchStopWatch.stop();
        log.error("Failed to process batch {}/{} - Error: {}", batch + 1, totalBatches,
            e.getMessage(), e);
        SleepUtil.sleep(1000);
      } finally {
        batchStopWatch.reset();
      }
    }

    stopWatch.stop();
    log.info(
        "Product search data initialization completed. Total initialized: {}/{}. Total duration: {}ms",
        initializedCount, totalProducts, stopWatch.getElapsedTimeMs());
  }

  @Async
  @Override
  public void initializeVectorStore() throws Exception {
    ProductVectorStoreService vectorStoreService = vectorStoreServiceProvider.getIfAvailable();
    if (vectorStoreService == null) {
      log.warn("ProductVectorStoreService is not available, skipping vector store initialization.");
      return;
    }

    long totalProducts = productRepository.countAll();
    log.info("Total products to initialize: {}", totalProducts);

    if (totalProducts == 0) {
      log.info("No products found in database, initialization completed");
      return;
    }

    int initializedCount = 0;
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

        vectorStoreService.addDocuments(products);
        initializedCount += products.size();

        batchStopWatch.stop();
        log.info(
            "Successfully processed batch {}/{}. Current initialized: {}/{}. Duration: {}ms",
            batch + 1, totalBatches, initializedCount, totalProducts,
            batchStopWatch.getElapsedTimeMs());

        SleepUtil.sleep(100);
      } catch (Exception e) {
        batchStopWatch.stop();
        log.error("Failed to process batch {}/{} - Error: {}", batch + 1, totalBatches,
            e.getMessage(), e);
        SleepUtil.sleep(1000);
      } finally {
        batchStopWatch.reset();
      }
    }

    stopWatch.stop();
    log.info(
        "Product vector store initialization completed. Total initialized: {}/{}. Total duration: {}ms",
        initializedCount, totalProducts, stopWatch.getElapsedTimeMs());
  }
}
