package org.atlas.services.catalog.infrastructure.search.elasticsearch.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.catalog.domain.entity.Product;
import org.atlas.services.catalog.infrastructure.search.elasticsearch.document.EsProduct;
import org.atlas.services.catalog.infrastructure.search.elasticsearch.mapper.EsProductMapper;
import org.atlas.services.catalog.infrastructure.search.elasticsearch.repository.EsProductRepository;
import org.atlas.services.catalog.port.out.search.ProductSearchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EsProductSearchServiceAdapter implements ProductSearchService {

  private final EsProductRepository elasticsearchProductRepository;
  private final ElasticsearchOperations elasticsearchOperations;

  private static final String INDEX_NAME = "product";

  /**
   * Create index if not exist
   */
  @Override
  public boolean createIndex() {
    IndexOperations indexOps = elasticsearchOperations.indexOps(EsProduct.class);
    if (!indexOps.exists()) {
      boolean created = indexOps.create();
      if (!created) {
        throw new RuntimeException("Failed to create search index: " + INDEX_NAME);
      }

      indexOps.putMapping();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public long countDocuments() {
    return elasticsearchProductRepository.count();
  }

  @Override
  public PagingResult<String> search(SearchProductCriteria criteria, PagingRequest pagingRequest) {
    Pageable pageable = PageRequest.of(pagingRequest.getPage(), pagingRequest.getSize());

    SearchHits<EsProduct> searchHits = elasticsearchProductRepository.search(criteria,
        pageable);

    List<String> matchedProductIds = searchHits.stream()
        .map(hit -> hit.getContent().getProductId())
        .distinct()
        .toList();

    return PagingResult.of(matchedProductIds, searchHits.getTotalHits(), pagingRequest);
  }

  @Override
  public void save(Product product) {
    EsProduct esProduct = EsProductMapper.INSTANCE.toProductDocument(product);
    elasticsearchProductRepository.save(esProduct);
  }

  @Override
  public void saveAll(List<Product> products) {
    if (CollectionUtil.isEmpty(products)) {
      return;
    }

    List<EsProduct> esProducts = products.stream()
        .map(EsProductMapper.INSTANCE::toProductDocument)
        .toList();

    elasticsearchProductRepository.saveAll(esProducts);
  }

  @Override
  public void delete(String productId) {
    EsProduct esProduct = elasticsearchProductRepository.findByProductId(productId)
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("Product %s does not exist in search index", productId)));

    elasticsearchProductRepository.deleteById(esProduct.getId());
  }
}
