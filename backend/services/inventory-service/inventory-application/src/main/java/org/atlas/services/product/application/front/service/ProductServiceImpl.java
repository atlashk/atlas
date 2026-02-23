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
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.product.port.in.model.RetrieveProductListInput;
import org.atlas.services.product.port.in.model.RetrieveProductListInput.Mode;
import org.atlas.services.product.port.in.service.ProductImageService;
import org.atlas.services.product.port.in.service.ProductService;
import org.atlas.services.product.port.out.fulltextsearch.FullTextSearchService;
import org.atlas.services.product.port.out.fulltextsearch.SearchProductCriteria;
import org.atlas.services.inventory.port.out.repository.StockRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

  private final StockRepository stockRepository;
  private final ProductImageService productImageService;
  private final ObjectProvider<FullTextSearchService> fullTextSearchServiceProvider;

  @Override
  public PagingResult<StockEntity> retrieveProductList(RetrieveProductListInput input) {
    PagingResult<StockEntity> productPage = input.getMode() == Mode.FULL_TEXT_SEARCH
        ? retrieveByFullTextSearch(input)
        : retrieveByDatabase(input);
    return attachImages(productPage);
  }

  private PagingResult<StockEntity> retrieveByFullTextSearch(RetrieveProductListInput input) {
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
    List<StockEntity> products = stockRepository.findByIdIn(
        matchedProductIdsPage.getData());
    if (CollectionUtil.isEmpty(products)) {
      return PagingResult.empty();
    }
    return PagingResult.of(products, matchedProductIdsPage.getPagination());
  }

  private PagingResult<StockEntity> retrieveByDatabase(RetrieveProductListInput input) {
    StockRepository.FindProductCriteria criteria = ProductMapper.INSTANCE.toFindProductCriteria(input);
    criteria.setStockStatus(ProductStockStatus.IN_STOCK);
    criteria.setIsActive(true);
    return stockRepository.findByCriteria(criteria, input.getPagingRequest());
  }

  private PagingResult<StockEntity> attachImages(PagingResult<StockEntity> productPage) {
    productPage.getData()
        .forEach(product -> product.setImage(productImageService.getImage(product.getId())));
    return productPage;
  }

  @Override
  @Cache(cacheName = "product", key = "#productId", ttl = 3600)
  public StockEntity retrieveProduct(String productId) throws Exception {
    // Get from DB
    StockEntity product = stockRepository.findById(productId)
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));

    // Set image
    product.setImage(productImageService.getImage(product.getId()));

    return product;
  }
}
