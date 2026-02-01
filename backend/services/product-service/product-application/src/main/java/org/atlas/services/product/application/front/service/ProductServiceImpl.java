package org.atlas.services.product.application.front.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cache.Cache;
import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.domain.product.ProductStatus;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.product.application.front.mapper.ProductMapper;
import org.atlas.services.product.port.in.front.model.RetrieveProductListInput;
import org.atlas.services.product.port.in.front.service.ProductImageService;
import org.atlas.services.product.port.in.front.service.ProductService;
import org.atlas.services.product.port.out.fulltextsearch.FullTextSearchService;
import org.atlas.services.product.port.out.fulltextsearch.SearchProductCriteria;
import org.atlas.services.product.port.out.repository.ProductRepository;
import org.atlas.services.product.port.out.repository.criteria.FindProductCriteria;
import org.atlas.services.product.domain.entity.Product;
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
        .forEach(product -> product.setImage(productImageService.getImage(product.getId())));

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
