package org.atlas.services.product.application.internal.service;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.services.product.application.internal.model.InternalRetrieveProductListInput;
import org.atlas.services.product.application.port.repository.ProductRepository;
import org.atlas.services.product.application.service.ProductImageService;
import org.atlas.services.product.domain.entity.Product;
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
    products.forEach(product -> product.setImage(productImageService.getImage(product.getId())));

    return products;
  }
}
