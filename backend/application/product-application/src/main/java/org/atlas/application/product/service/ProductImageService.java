package org.atlas.application.product.service;

import java.io.IOException;

public interface ProductImageService {

  void uploadImage(Integer productId, byte[] imageBytes, String imageContentType)
      throws IOException;

  String getImage(Integer productId) throws IOException;

  void deleteImage(Integer productId) throws IOException;
}
