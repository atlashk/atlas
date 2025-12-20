package org.atlas.domain.product.usecase.front.handler;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.infrastructure.fulltextsearch.FullTextSearchService;
import org.atlas.domain.product.infrastructure.fulltextsearch.SearchProductCriteria;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.domain.product.service.ProductImageService;
import org.atlas.domain.product.shared.ProductStatus;
import org.atlas.domain.product.usecase.front.mapper.ProductMapper;
import org.atlas.domain.product.usecase.front.model.SearchProductInput;
import org.atlas.framework.collection.CollectionUtil;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.util.StringUtil;
import org.springframework.beans.factory.ObjectProvider;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class SearchProductUseCaseHandler {

  private final ObjectProvider<FullTextSearchService> fullTextSearchServiceProvider;
  private final ProductRepository productRepository;
  private final ProductImageService productImageService;

  public PagingResult<Product> handle(SearchProductInput input) throws Exception {
    PagingResult<Product> productPage;

    FullTextSearchService fullTextSearchService = fullTextSearchServiceProvider.getIfAvailable();
    if (fullTextSearchService != null) {
      // Using full-text search engine
      SearchProductCriteria criteria = ProductMapper.INSTANCE.toSearchProductCriteria(input);
      PagingResult<Integer> matchedProductIdsPage = fullTextSearchService.search(criteria,
          input.getPagingRequest());

      if (matchedProductIdsPage.checkEmpty()) {
        return PagingResult.empty();
      }

      // Find products from DB again
      List<Product> products = productRepository.findByIdIn(matchedProductIdsPage.getData());
      if (CollectionUtil.isEmpty(products)) {
        return PagingResult.empty();
      }

      productPage = PagingResult.of(products, matchedProductIdsPage.getPagination());
    } else {
      // Using DB dynamic query natively
      FindProductCriteria criteria = ProductMapper.INSTANCE.toFindProductCriteria(input);
      criteria.setStatus(ProductStatus.IN_STOCK);
      criteria.setIsActive(true);
      productPage = productRepository.findByCriteria(criteria, input.getPagingRequest());
    }

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
