package org.atlas.infrastructure.storage.filesystem;

import org.atlas.framework.storage.StorageConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FilesystemStorageConfig implements StorageConfig {

  @Value("${FILESYSTEM_PRODUCT_IMAGE_BUCKET:product_image}")
  private String productImageBucket;

  @Override
  public String getProductImageBucket() {
    return productImageBucket;
  }
}
