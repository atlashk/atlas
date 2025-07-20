package org.atlas.domain.product.service;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.config.Application;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.service.DomainService;
import org.atlas.framework.storage.StoragePort;
import org.atlas.framework.storage.model.DeleteFileRequest;
import org.atlas.framework.storage.model.GetFileRequest;
import org.atlas.framework.storage.model.UploadFileRequest;
import org.atlas.framework.util.ImageUtil;
import org.atlas.framework.util.StringUtil;

@DomainService
@RequiredArgsConstructor
@Slf4j
public class ProductImageService {

  private final ApplicationConfigPort applicationConfigPort;
  private final StoragePort storagePort;

  public void uploadImage(Integer productId, String base64St) {
    String bucket = getBucket();
    String objectKey = getObjectKey(productId);
    byte[] fileContent = ImageUtil.fromBase64(base64St);
    UploadFileRequest storageRequest = new UploadFileRequest(bucket, objectKey, fileContent);
    try {
      storagePort.uploadFile(storageRequest);
    } catch (IOException e) {
      log.error("Failed to upload image for product {}", productId, e);
    }
  }

  public String getImage(Integer productId) {
    String bucket = getBucket();
    String objectKey = getObjectKey(productId);
    GetFileRequest storageRequest = new GetFileRequest(bucket, objectKey);
    try {
      byte[] fileContent = storagePort.getFile(storageRequest);
      return ImageUtil.toBase64(fileContent);
    } catch (IOException e) {
      return StringUtil.EMPTY;
    }
  }

  public void deleteImage(Integer productId) {
    String bucket = getBucket();
    String objectKey = getObjectKey(productId);
    DeleteFileRequest storageRequest = new DeleteFileRequest(bucket, objectKey);
    try {
      storagePort.deleteFile(storageRequest);
    } catch (IOException e) {
      log.error("Failed to delete image for product {}", productId, e);
    }
  }

  private String getBucket() {
    String bucket = applicationConfigPort.getConfig(Application.PRODUCT_SERVICE,
        "product-images-bucket");
    if (StringUtil.isBlank(bucket)) {
      throw new IllegalArgumentException("No bucket configured");
    }
    return bucket;
  }

  private String getObjectKey(Integer productId) {
    return String.format("%d.jpg", productId);
  }
}
