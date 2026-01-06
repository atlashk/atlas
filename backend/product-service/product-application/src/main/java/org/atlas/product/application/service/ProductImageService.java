package org.atlas.product.application.service;

import java.io.IOException;

public interface ProductImageService {

  void uploadImage(Integer productId, byte[] imageBytes, String imageContentType)
      throws IOException;

  String getImage(Integer productId);

  void deleteImage(Integer productId);
}
