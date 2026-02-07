package org.atlas.services.product.application.internal.service;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.port.in.internal.model.InternalRetrieveProductListInput;
import org.atlas.services.product.port.in.internal.service.InternalProductService;
import org.atlas.services.product.port.out.repository.ProductRepository;
import org.atlas.services.product.port.in.front.service.ProductImageService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalProductServiceImpl implements InternalProductService {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;

  @Override
  public List<ProductEntity> retrieveProductList(InternalRetrieveProductListInput input) {
    List<ProductEntity> products = productRepository.findByIdIn(input.getIds());
    if (CollectionUtil.isEmpty(products)) {
      return Collections.emptyList();
    }

    // Update image
    products.forEach(
        product -> product.setImage(productImageService.getImage(product.getId())));

    return products;
  }
}
