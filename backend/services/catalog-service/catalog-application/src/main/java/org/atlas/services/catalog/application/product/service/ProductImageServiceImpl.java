package org.atlas.services.catalog.application.product.service;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.image.ImageUtil;
import org.atlas.libs.framework.storage.StorageConstant;
import org.atlas.libs.framework.storage.StorageService;
import org.atlas.libs.framework.storage.model.DeleteFileRequest;
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
    GetFileRequest storageRequest = GetFileRequest.builder()
        .bucket(bucket)
        .objectKey(objectKey)
        .build();
    byte[] fileContent;
    try {
      fileContent = storageService.getFile(storageRequest);
    } catch (IOException e) {
      return StringUtil.EMPTY;
    }
    if (ArrayUtil.isEmpty(fileContent)) {
      return StringUtil.EMPTY;
    }
    return ImageUtil.toBase64(fileContent);
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
}
