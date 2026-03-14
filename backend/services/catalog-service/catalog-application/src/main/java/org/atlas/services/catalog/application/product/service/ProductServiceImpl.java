package org.atlas.services.catalog.application.product.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cache.Cache;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.catalog.application.product.mapper.ProductMapper;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.domain.error.DomainError;
import org.atlas.services.catalog.domain.exception.DomainException;
import org.atlas.services.catalog.port.in.product.model.RetrieveProductListInput;
import org.atlas.services.catalog.port.in.product.model.RetrieveProductListInput.Mode;
import org.atlas.services.catalog.port.in.product.service.ProductImageService;
import org.atlas.services.catalog.port.in.product.service.ProductService;
import org.atlas.services.catalog.port.out.search.SearchService;
import org.atlas.services.catalog.port.out.search.SearchProductCriteria;
import org.atlas.services.catalog.port.out.repository.ProductRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ObjectProvider<SearchService> searchServiceProvider;

  @Override
  public PagingResult<ProductEntity> retrieveProductList(RetrieveProductListInput input) {
    PagingResult<ProductEntity> productPage = input.getMode() == Mode.SEARCH
        ? retrieveBySearch(input) : retrieveByDatabase(input);

    attachImages(productPage);

    return productPage;
  }

  private PagingResult<ProductEntity> retrieveBySearch(RetrieveProductListInput input) {
    SearchService searchService = searchServiceProvider.getIfAvailable();
    if (searchService == null) {
      log.warn("Search service is not available, fallback to database query");
      return retrieveByDatabase(input);
    }
    SearchProductCriteria criteria = ProductMapper.INSTANCE.toSearchProductCriteria(input);
    PagingResult<String> matchedProductIdsPage = searchService.search(criteria,
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
    ProductRepository.FindProductCriteria criteria = ProductMapper.INSTANCE.toFindProductCriteria(
        input);
    criteria.setInStock(true);
    return productRepository.findByCriteria(criteria, input.getPagingRequest());
  }

  private void attachImages(PagingResult<ProductEntity> productPage) {
    productPage.getData()
        .forEach(product -> product.setImage(productImageService.getImage(product.getId())));
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
