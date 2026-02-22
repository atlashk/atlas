package org.atlas.services.product.port.in.front.service;

import java.io.IOException;

public interface ProductImageService {

  void uploadImage(String productId, byte[] imageBytes, String imageContentType)
      throws IOException;

  String getImage(String productId);

  void deleteImage(String productId);
}
