package org.atlas.application.product.service;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.application.product.mapper.ProductMapper;
import org.atlas.application.product.model.RetrieveProductListInput;
import org.atlas.application.product.port.fulltextsearch.FullTextSearchService;
import org.atlas.application.product.port.fulltextsearch.SearchProductCriteria;
import org.atlas.application.product.port.repository.ProductRepository;
import org.atlas.application.product.port.repository.criteria.FindProductCriteria;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.shared.ProductStatus;
import org.atlas.framework.cache.Cache;
import org.atlas.framework.collection.CollectionUtil;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.util.StringUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ObjectProvider<FullTextSearchService> fullTextSearchServiceProvider;

  @Override
  public PagingResult<Product> retrieveProductList(RetrieveProductListInput input) {
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

  @Override
  @Cache(cacheName = "product", key = "#productId", ttl = 3600)
  public Product retrieveProduct(Integer productId) throws Exception {
    // Get from DB
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));

    // Set image
    product.setImage(productImageService.getImage(product.getId()));

    return product;
  }
}
