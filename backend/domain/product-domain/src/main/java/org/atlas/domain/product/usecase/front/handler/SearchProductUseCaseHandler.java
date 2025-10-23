package org.atlas.domain.product.usecase.front.handler;

import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.infrastructure.search.SearchProductCriteria;
import org.atlas.domain.product.infrastructure.search.SearchService;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.domain.product.shared.ProductStatus;
import org.atlas.domain.product.usecase.front.model.SearchProductInput;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.util.CollectionUtil;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class SearchProductUseCaseHandler {

  private final @Nullable SearchService searchService;
  private final ProductRepository productRepository;
  private final ProductImageService productImageService;

  public PagingResult<ProductEntity> handle(SearchProductInput input) throws Exception {
    PagingResult<ProductEntity> productPage;
    if (searchService != null) {
      // Using search engine
      SearchProductCriteria criteria = ObjectMapperUtil.getInstance()
          .map(input, SearchProductCriteria.class);
      PagingResult<Integer> matchedProductIdsPage = searchService.search(criteria,
          input.getPagingRequest());

      if (matchedProductIdsPage.checkEmpty()) {
        return PagingResult.empty();
      }

      // Find products from DB again
      List<ProductEntity> products = productRepository.findByIdIn(matchedProductIdsPage.getData());
      if (CollectionUtil.isEmpty(products)) {
        return PagingResult.empty();
      }

      productPage = PagingResult.of(products, matchedProductIdsPage.getPagination());
    } else {
      // Using DB dynamic query natively
      FindProductCriteria criteria = ObjectMapperUtil.getInstance()
          .map(input, FindProductCriteria.class);
      criteria.setStatus(ProductStatus.IN_STOCK);
      criteria.setIsActive(true);
      productPage = productRepository.findByCriteria(criteria, input.getPagingRequest());
    }

    // Set image
    productPage.getData()
        .forEach(product ->
            product.setImage(productImageService.getImage(product.getId())));

    return productPage;
  }
}
