package org.atlas.domain.product.usecase.front.handler;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.domain.product.shared.ProductStatus;
import org.atlas.domain.product.usecase.front.model.FrontSearchProductInput;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.search.SearchService;

@UseCaseHandler
@RequiredArgsConstructor
public class FrontSearchProductUseCaseHandler {

  private final @Nullable SearchService searchService;
  private final ProductRepository productRepository;
  private final ProductImageService productImageService;

  public PagingResult<ProductEntity> handle(FrontSearchProductInput input) throws Exception {
    PagingResult<ProductEntity> productPage = null;
    if (searchService != null) {
      // Using search engine
//      SearchCriteria criteria = ObjectMapperUtil.getInstance()
//          .map(input, SearchCriteria.class);
//      productPage = searchPort.search(criteria, input.getPagingRequest());
    } else {
      // Using DB
      FindProductCriteria criteria = ObjectMapperUtil.getInstance()
          .map(input, FindProductCriteria.class);
      criteria.setStatus(ProductStatus.IN_STOCK);
      criteria.setIsActive(true);
      productPage = productRepository.findByCriteria(criteria, input.getPagingRequest());
    }

    if (productPage == null) {
      return PagingResult.empty();
    }

    // Set image
    productPage.getData()
        .forEach(product ->
            product.setImage(productImageService.getImage(product.getId())));

    return productPage;
  }
}
