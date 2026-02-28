package org.atlas.services.catalog.application.product.service;

import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.image.ImageUtil;
import org.atlas.libs.framework.storage.StorageConstant;
import org.atlas.libs.framework.storage.StorageService;
import org.atlas.libs.framework.storage.model.CheckExistRequest;
import org.atlas.libs.framework.storage.model.DeleteFileRequest;
import org.atlas.libs.framework.storage.model.GetDownloadUrlRequest;
import org.atlas.libs.framework.storage.model.GetFileRequest;
import org.atlas.libs.framework.storage.model.UploadFileRequest;
import org.atlas.libs.framework.util.ArrayUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.catalog.port.in.product.service.ProductImageService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageServiceImpl implements ProductImageService {

  private final StorageService storageService;

  @Override
  public void uploadImage(String productId, byte[] imageBytes, String imageContentType)
      throws IOException {
    String bucket = StorageConstant.PRODUCT_IMAGE_BUCKET;
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
    String bucket = StorageConstant.PRODUCT_IMAGE_BUCKET;
    String objectKey = getObjectKey(productId);

    if (!checkExist(bucket, objectKey)) {
      return StringUtil.EMPTY;
    }

    // First, try to get a downloadable URL
    String downloadUrl = tryGetDownloadUrl(bucket, objectKey, productId);
    if (StringUtil.isNotBlank(downloadUrl)) {
      return downloadUrl;
    }

    // Fallback to fetching the file content directly if URL is unavailable
    return tryGetFileContentAsBase64(bucket, objectKey, productId);
  }

  @Override
  public void deleteImage(String productId) {
    String bucket = StorageConstant.PRODUCT_IMAGE_BUCKET;
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

  private boolean checkExist(String bucket, String objectKey) {
    CheckExistRequest checkExistRequest = CheckExistRequest.builder()
        .bucket(bucket)
        .objectKey(objectKey)
        .build();
    return storageService.checkExist(checkExistRequest);
  }

  /**
   * Tries to get a presigned download URL for the image.
   *
   * @param bucket    The storage bucket.
   * @param objectKey The object key.
   * @param productId The product ID for logging.
   * @return The download URL or an empty string if it fails.
   */
  private String tryGetDownloadUrl(String bucket, String objectKey, String productId) {
    try {
      GetDownloadUrlRequest urlRequest = GetDownloadUrlRequest.builder()
          .bucket(bucket)
          .objectKey(objectKey)
          .ttl(Duration.ofHours(1)) // Keep the URL valid for 1 hour
          .build();
      return storageService.getDownloadUrl(urlRequest);
    } catch (IOException e) {
      log.warn("Failed to get download URL for productId='{}': {}", productId, e.getMessage());
      return StringUtil.EMPTY;
    }
  }

  /**
   * Tries to get the raw file content and encodes it as Base64.
   *
   * @param bucket    The storage bucket.
   * @param objectKey The object key.
   * @param productId The product ID for logging.
   * @return The Base64-encoded image content or an empty string if it fails.
   */
  private String tryGetFileContentAsBase64(String bucket, String objectKey, String productId) {
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
}
