package org.atlas.domain.product.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.domain.product.usecase.admin.mapper.AdminProductMapper;
import org.atlas.domain.product.usecase.admin.model.AdminListProductInput;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;
import org.atlas.framework.paging.PagingResult;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class AdminListProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;

  public PagingResult<Product> handle(AdminListProductInput input) throws Exception {
    FindProductCriteria criteria = AdminProductMapper.INSTANCE.toFindProductCriteria(input);
    PagingResult<Product> productPage = productRepository.findByCriteria(criteria,
        input.getPagingRequest());

    // Set image
    productPage.getData()
        .forEach(product ->
            product.setImage(productImageService.getImage(product.getId())));

    return productPage;
  }
}
