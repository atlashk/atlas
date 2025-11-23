package org.atlas.domain.product.usecase.internal.handler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.domain.product.usecase.internal.model.InternalListProductInput;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;
import org.atlas.framework.util.CollectionUtil;
import org.atlas.framework.util.StringUtil;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class InternalListProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;

  public List<Product> handle(InternalListProductInput input) throws Exception {
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
