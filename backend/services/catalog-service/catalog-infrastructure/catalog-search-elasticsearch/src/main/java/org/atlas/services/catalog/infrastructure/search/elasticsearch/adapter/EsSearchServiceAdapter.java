package org.atlas.services.catalog.infrastructure.search.elasticsearch.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.infrastructure.search.elasticsearch.document.EsProduct;
import org.atlas.services.catalog.infrastructure.search.elasticsearch.mapper.EsProductMapper;
import org.atlas.services.catalog.infrastructure.search.elasticsearch.repository.EsProductRepository;
import org.atlas.services.catalog.port.out.search.SearchIndex;
import org.atlas.services.catalog.port.out.search.SearchProductCriteria;
import org.atlas.services.catalog.port.out.search.SearchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EsSearchServiceAdapter implements SearchService {

  private final EsProductRepository elasticsearchProductRepository;
  private final ElasticsearchOperations elasticsearchOperations;

  @Override
  public boolean createIndex(SearchIndex index) {
    if (index == null) {
      throw new IllegalArgumentException("Search index cannot be null");
    }

    IndexOperations indexOps = createIndexOperation(index);

    if (!indexOps.exists()) {
      boolean created = indexOps.create();
      if (!created) {
        throw new RuntimeException("Failed to create search index: " + index);
      }

      indexOps.putMapping();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public long countDocuments(SearchIndex index) {
    return elasticsearchProductRepository.count();
  }

  @Override
  public PagingResult<String> search(SearchProductCriteria criteria,
      PagingRequest pagingRequest) {
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
  public void save(ProductEntity product) {
    EsProduct esProduct = EsProductMapper.INSTANCE.toProductDocument(product);
    elasticsearchProductRepository.save(esProduct);
  }

  @Override
  public void saveAll(List<ProductEntity> products) {
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

  private IndexOperations createIndexOperation(SearchIndex index) {
    IndexOperations indexOperations;
    if (SearchIndex.PRODUCT.equals(index)) {
      indexOperations = elasticsearchOperations.indexOps(EsProduct.class);
    } else {
      throw new UnsupportedOperationException(
          String.format("Search index %s is not supported yet", index));
    }
    return indexOperations;
  }
}
