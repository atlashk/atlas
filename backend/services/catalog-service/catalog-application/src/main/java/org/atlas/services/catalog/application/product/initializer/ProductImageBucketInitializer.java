package org.atlas.services.catalog.application.product.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.storage.StorageService;
import org.atlas.services.catalog.port.out.storage.ProductStorageConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductImageBucketInitializer {

  private final StorageService storageService;

  @EventListener(ApplicationReadyEvent.class)
  public void initialize(ApplicationReadyEvent event) {
    try {
      storageService.createBucket(ProductStorageConstant.PRODUCT_IMAGE_BUCKET);
      log.info("The bucket of product image has been created.");
    } catch (Exception e) {
      // Fail-fast
      log.error("Failed to create the bucket of product image: {}", e.getMessage(), e);
      SpringApplication.exit(event.getApplicationContext());
    }
  }
}
