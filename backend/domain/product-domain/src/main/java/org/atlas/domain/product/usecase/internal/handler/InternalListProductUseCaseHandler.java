package org.atlas.domain.product.usecase.internal.handler;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.domain.product.usecase.internal.model.InternalListProductInput;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.util.CollectionUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class InternalListProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;

  public List<ProductEntity> handle(InternalListProductInput input) throws Exception {
    List<ProductEntity> productEntities = productRepository.findByIdIn(input.getIds());
    if (CollectionUtil.isEmpty(productEntities)) {
      return Collections.emptyList();
    }

    // Update image
    productEntities.forEach(productEntity -> {
      productEntity.setImage(productImageService.getImage(productEntity.getId()));
    });

    return productEntities;
  }
}
