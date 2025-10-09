package org.atlas.domain.product.usecase.internal.handler;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.domain.product.usecase.internal.model.InternalListProductInput;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.util.CollectionUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class InternalListProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;

  public List<ProductEntity> handle(InternalListProductInput input) throws Exception {
    List<ProductEntity> products = productRepository.findByIdIn(input.getIds());
    if (CollectionUtil.isEmpty(products)) {
      return Collections.emptyList();
    }

    // Update image
    products.forEach(product -> {
      product.setImage(productImageService.getImage(product.getId()));
    });

    return products;
  }
}
