package org.atlas.domain.product.usecase.admin.handler;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.domain.product.usecase.admin.mapper.AdminProductMapper;
import org.atlas.domain.product.usecase.admin.model.AdminListProductInput;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.util.StringUtil;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class AdminListProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;

  public PagingResult<Product> handle(AdminListProductInput input) throws Exception {
    FindProductCriteria criteria = AdminProductMapper.INSTANCE.toFindProductCriteria(input);
    PagingResult<Product> productPage = productRepository.findByCriteria(criteria,
        input.getPagingRequest());

    // Set image
    productPage.getData()
        .forEach(product -> {
          try {
            product.setImage(productImageService.getImage(product.getId()));
          } catch (IOException e) {
            log.error("Failed to get product image: productId={}, error={}",
                product.getId(), e.getMessage());
            product.setImage(StringUtil.EMPTY);
          }
        });

    return productPage;
  }
}
