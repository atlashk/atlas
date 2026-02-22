package org.atlas.services.product.application.front.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cache.Cache;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.domain.catalog.ProductStockStatus;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.product.application.front.mapper.ProductMapper;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.port.in.front.model.RetrieveProductListInput;
import org.atlas.services.product.port.in.front.model.RetrieveProductListInput.Mode;
import org.atlas.services.product.port.in.front.service.ProductImageService;
import org.atlas.services.product.port.in.front.service.ProductService;
import org.atlas.services.product.port.out.fulltextsearch.FullTextSearchService;
import org.atlas.services.product.port.out.fulltextsearch.SearchProductCriteria;
import org.atlas.services.product.port.out.repository.ProductRepository;
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
  public PagingResult<ProductEntity> retrieveProductList(RetrieveProductListInput input) {
    PagingResult<ProductEntity> productPage = input.getMode() == Mode.FULL_TEXT_SEARCH
        ? retrieveByFullTextSearch(input)
        : retrieveByDatabase(input);
    return attachImages(productPage);
  }

  private PagingResult<ProductEntity> retrieveByFullTextSearch(RetrieveProductListInput input) {
    FullTextSearchService fullTextSearchService = fullTextSearchServiceProvider.getIfAvailable();
    if (fullTextSearchService == null) {
      log.warn("Full-text search service is not available, fallback to database query");
      return retrieveByDatabase(input);
    }
    SearchProductCriteria criteria = ProductMapper.INSTANCE.toSearchProductCriteria(input);
    PagingResult<String> matchedProductIdsPage = fullTextSearchService.search(criteria,
        input.getPagingRequest());
    if (matchedProductIdsPage.checkEmpty()) {
      return PagingResult.empty();
    }
    List<ProductEntity> products = productRepository.findByIdIn(
        matchedProductIdsPage.getData());
    if (CollectionUtil.isEmpty(products)) {
      return PagingResult.empty();
    }
    return PagingResult.of(products, matchedProductIdsPage.getPagination());
  }

  private PagingResult<ProductEntity> retrieveByDatabase(RetrieveProductListInput input) {
    ProductRepository.FindProductCriteria criteria = ProductMapper.INSTANCE.toFindProductCriteria(input);
    criteria.setStockStatus(ProductStockStatus.IN_STOCK);
    criteria.setIsActive(true);
    return productRepository.findByCriteria(criteria, input.getPagingRequest());
  }

  private PagingResult<ProductEntity> attachImages(PagingResult<ProductEntity> productPage) {
    productPage.getData()
        .forEach(product -> product.setImage(productImageService.getImage(product.getId())));
    return productPage;
  }

  @Override
  @Cache(cacheName = "product", key = "#productId", ttl = 3600)
  public ProductEntity retrieveProduct(String productId) throws Exception {
    // Get from DB
    ProductEntity product = productRepository.findById(productId)
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));

    // Set image
    product.setImage(productImageService.getImage(product.getId()));

    return product;
  }
}
