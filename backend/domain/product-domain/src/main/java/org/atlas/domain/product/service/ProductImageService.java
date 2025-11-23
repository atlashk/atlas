package org.atlas.domain.product.service;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.storage.StorageConstant;
import org.atlas.framework.storage.StorageService;
import org.atlas.framework.storage.model.DeleteFileRequest;
import org.atlas.framework.storage.model.GetFileRequest;
import org.atlas.framework.storage.model.UploadFileRequest;
import org.atlas.framework.util.ArrayUtil;
import org.atlas.framework.util.ImageUtil;
import org.atlas.framework.util.StringUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageService {

  private final StorageService storageService;

  public void uploadImage(Integer productId, byte[] imageBytes, String imageContentType)
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

  public String getImage(Integer productId) throws IOException {
    String bucket = StorageConstant.PRODUCT_IMAGE_BUCKET;
    String objectKey = getObjectKey(productId);
    GetFileRequest storageRequest = GetFileRequest.builder()
        .bucket(bucket)
        .objectKey(objectKey)
        .build();
    byte[] fileContent = storageService.getFile(storageRequest);
    if (ArrayUtil.isEmpty(fileContent)) {
      return StringUtil.EMPTY;
    }
    return ImageUtil.toBase64(fileContent);
  }

  public void deleteImage(Integer productId) throws IOException {
    String bucket = StorageConstant.PRODUCT_IMAGE_BUCKET;
    String objectKey = getObjectKey(productId);
    DeleteFileRequest storageRequest = DeleteFileRequest.builder()
        .bucket(bucket)
        .objectKey(objectKey)
        .build();
    storageService.deleteFile(storageRequest);
    log.info("Deleted product image: productId={}", productId);
  }

  private String getObjectKey(Integer productId) {
    return String.format("%d.jpg", productId);
  }
}
