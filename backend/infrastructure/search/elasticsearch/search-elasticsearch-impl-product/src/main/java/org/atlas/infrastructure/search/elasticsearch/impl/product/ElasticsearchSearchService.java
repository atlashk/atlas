package org.atlas.infrastructure.search.elasticsearch.impl.product;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.infrastructure.search.SearchIndex;
import org.atlas.domain.product.infrastructure.search.SearchProductCriteria;
import org.atlas.domain.product.infrastructure.search.SearchService;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.util.CollectionUtil;
import org.atlas.infrastructure.search.elasticsearch.impl.product.document.ElasticsearchProduct;
import org.atlas.infrastructure.search.elasticsearch.impl.product.mapper.ElasticsearchProductMapper;
import org.atlas.infrastructure.search.elasticsearch.impl.product.repository.ElasticsearchProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElasticsearchSearchService implements SearchService {

  private final ElasticsearchProductRepository elasticsearchProductRepository;
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
  public PagingResult<Integer> search(SearchProductCriteria criteria,
      PagingRequest pagingRequest) {
    Pageable pageable = PageRequest.of(pagingRequest.getPage(), pagingRequest.getSize());

    SearchHits<ElasticsearchProduct> searchHits = elasticsearchProductRepository.search(criteria,
        pageable);

    List<Integer> matchedProductIds = searchHits.stream()
        .map(hit -> hit.getContent().getProductId())
        .distinct()
        .toList();

    return PagingResult.of(matchedProductIds, searchHits.getTotalHits(), pagingRequest);
  }

  @Override
  public void saveAll(List<Product> products) {
    if (CollectionUtil.isEmpty(products)) {
      return;
    }

    List<ElasticsearchProduct> elasticsearchProducts = products.stream()
        .map(ElasticsearchProductMapper.INSTANCE::toProductDocument)
        .toList();

    elasticsearchProductRepository.saveAll(elasticsearchProducts);
  }

  @Override
  public void deleteProduct(Integer productId) {
    ElasticsearchProduct elasticsearchProduct = elasticsearchProductRepository.findByProductId(
            productId)
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("Product %d does not exist in search index", productId)));

    elasticsearchProductRepository.deleteById(elasticsearchProduct.getId());
  }

  private IndexOperations createIndexOperation(SearchIndex index) {
    IndexOperations indexOperations;
    if (SearchIndex.PRODUCT.equals(index)) {
      indexOperations = elasticsearchOperations.indexOps(ElasticsearchProduct.class);
    } else {
      throw new UnsupportedOperationException(
          String.format("Search index %s is not supported yet", index));
    }
    return indexOperations;
  }
}
