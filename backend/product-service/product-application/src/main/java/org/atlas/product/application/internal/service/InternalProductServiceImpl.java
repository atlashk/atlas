package org.atlas.product.application.internal.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.product.application.internal.model.InternalRetrieveProductListInput;
import org.atlas.product.application.port.repository.ProductRepository;
import org.atlas.product.application.service.ProductImageService;
import org.atlas.product.domain.entity.Product;
import org.atlas.common.framework.collection.CollectionUtil;
import org.atlas.common.framework.util.StringUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalProductServiceImpl implements InternalProductService {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;

  @Override
  public List<Product> retrieveProductList(InternalRetrieveProductListInput input) {
    List<Product> products = productRepository.findByIdIn(input.getIds());
    if (CollectionUtil.isEmpty(products)) {
      return Collections.emptyList();
    }

    // Update image
    products.forEach(product -> {
      try {
        product.setImage(productImageService.getImage(product.getId()));
      } catch (IOException e) {
        log.error("Failed to get product image: productId={}, error={}",
            product.getId(), e.getMessage());
        product.setImage(StringUtil.EMPTY);
      }
    });

    return products;
  }
}
