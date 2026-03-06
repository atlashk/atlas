package org.atlas.services.catalog.application.product.service;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.util.ImageUtil;
import org.atlas.libs.framework.storage.StorageService;
import org.atlas.libs.framework.storage.model.CheckFileExistsRequest;
import org.atlas.libs.framework.storage.model.DeleteFileRequest;
import org.atlas.libs.framework.storage.model.GetFileRequest;
import org.atlas.libs.framework.storage.model.UploadFileRequest;
import org.atlas.libs.framework.util.ArrayUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.catalog.port.in.product.service.ProductImageService;
import org.atlas.services.catalog.port.out.storage.ProductStorageConstant;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageServiceImpl implements ProductImageService {

  private final StorageService storageService;

  @Override
  public void uploadImage(String productId, byte[] imageBytes, String imageContentType)
      throws IOException {
    String bucket = ProductStorageConstant.PRODUCT_IMAGE_BUCKET;
    String objectKey = getObjectKey(productId);
    UploadFileRequest storageRequest = UploadFileRequest.builder()
        .bucket(bucket)
        .objectKey(objectKey)
        .bytes(imageBytes)
        .contentType(imageContentType)
        .build();
    storageService.uploadFile(storageRequest);
    log.info("Uploaded product image: productId={}", productId);
  }

  @Override
  public String getImage(String productId) {
    String bucket = ProductStorageConstant.PRODUCT_IMAGE_BUCKET;
    String objectKey = getObjectKey(productId);

    if (!checkImageExists(bucket, objectKey)) {
      return StringUtil.EMPTY;
    }

    GetFileRequest contentRequest = GetFileRequest.builder()
        .bucket(bucket)
        .objectKey(objectKey)
        .build();
    try {
      byte[] fileContent = storageService.getFileContent(contentRequest);
      if (ArrayUtil.isEmpty(fileContent)) {
        log.warn("File content is empty for productId='{}'.", productId);
        return StringUtil.EMPTY;
      }
      return ImageUtil.toBase64(fileContent);
    } catch (IOException e) {
      log.warn("Failed to get file content for productId='{}': {}", productId, e.getMessage());
      return StringUtil.EMPTY;
    }
  }

  @Override
  public void deleteImage(String productId) {
    String bucket = ProductStorageConstant.PRODUCT_IMAGE_BUCKET;
    String objectKey = getObjectKey(productId);
    DeleteFileRequest storageRequest = DeleteFileRequest.builder()
        .bucket(bucket)
        .objectKey(objectKey)
        .build();
    try {
      storageService.deleteFile(storageRequest);
      log.info("Deleted product image: productId={}", productId);
    } catch (IOException e) {
      log.error("Failed to delete image: productId={}", productId);
    }
  }

  private String getObjectKey(String productId) {
    return String.format("%s.jpg", productId);
  }

  private boolean checkImageExists(String bucket, String objectKey) {
    CheckFileExistsRequest checkFileExistsRequest = CheckFileExistsRequest.builder()
        .bucket(bucket)
        .objectKey(objectKey)
        .build();
    return storageService.checkFileExists(checkFileExistsRequest);
  }
}
