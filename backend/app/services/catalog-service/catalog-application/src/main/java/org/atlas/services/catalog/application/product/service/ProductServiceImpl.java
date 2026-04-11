package org.atlas.services.catalog.application.product.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cache.Cache;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.catalog.application.product.mapper.ProductMapper;
import org.atlas.services.catalog.domain.entity.Product;
import org.atlas.services.catalog.domain.error.CatalogDomainError;
import org.atlas.services.catalog.port.in.product.model.RetrieveProductListInput;
import org.atlas.services.catalog.port.in.product.model.RetrieveProductListInput.Mode;
import org.atlas.services.catalog.port.in.product.service.ProductImageService;
import org.atlas.services.catalog.port.in.product.service.ProductService;
import org.atlas.services.catalog.port.out.repository.ProductRepository;
import org.atlas.services.catalog.port.out.search.ProductSearchService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ObjectProvider<ProductSearchService> searchServiceProvider;

  @Override
  public PagingResult<Product> retrieveProductList(RetrieveProductListInput input) {
    PagingResult<Product> productPage;
    if (Objects.requireNonNull(input.getMode()) == Mode.SEARCH) {
      productPage = retrieveBySearch(input);
    } else {
      productPage = retrieveByDatabase(input);
    }

    attachImages(productPage);

    return productPage;
  }

  private PagingResult<Product> retrieveBySearch(RetrieveProductListInput input) {
    ProductSearchService productSearchService = searchServiceProvider.getIfAvailable();
    if (productSearchService == null) {
      log.warn("Search service is not available, fallback to database query");
      return retrieveByDatabase(input);
    }

    ProductSearchService.SearchProductCriteria criteria =
        ProductMapper.INSTANCE.toSearchProductCriteria(input);
    PagingResult<String> matchedProductIdsPage = productSearchService.search(criteria,
        input.getPagingRequest());

    if (matchedProductIdsPage.checkEmpty()) {
      return PagingResult.empty();
    }

    List<Product> products = productRepository.findByIdIn(
        matchedProductIdsPage.getData());
    if (CollectionUtil.isEmpty(products)) {
      return PagingResult.empty();
    }
    return PagingResult.of(products, matchedProductIdsPage.getPagination());
  }

  private PagingResult<Product> retrieveByDatabase(RetrieveProductListInput input) {
    ProductRepository.FindProductCriteria criteria = ProductMapper.INSTANCE.toFindProductCriteria(
        input);
    criteria.setInStock(true);
    return productRepository.findByCriteria(criteria, input.getPagingRequest());
  }

  private void attachImages(PagingResult<Product> productPage) {
    productPage.getData()
        .forEach(product -> product.setImage(productImageService.getImage(product.getId())));
  }

  @Override
  @Cache(name = "product", key = "#productId", ttl = 3600)
  public Product retrieveProduct(String productId) throws Exception {
    // Get from DB
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new DomainException(CatalogDomainError.PRODUCT_NOT_FOUND));

    // Set image
    product.setImage(productImageService.getImage(product.getId()));

    return product;
  }
}
