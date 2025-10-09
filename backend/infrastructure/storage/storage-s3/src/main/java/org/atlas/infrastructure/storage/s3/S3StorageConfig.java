package org.atlas.infrastructure.storage.s3;

import org.atlas.framework.storage.config.StorageConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class S3StorageConfig implements StorageConfig {

  @Value("${S3_PRODUCT_IMAGE_BUCKET:product_image}")
  private String productImageBucket;

  @Override
  public String getProductImageBucket() {
    return productImageBucket;
  }
}
