package org.atlas.infrastructure.search.elasticsearch.impl.product.repository;

import org.atlas.domain.product.infrastructure.search.SearchProductCriteria;
import org.atlas.infrastructure.search.elasticsearch.impl.product.document.ElasticsearchProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface CustomElasticsearchProductRepository {

  SearchHits<ElasticsearchProduct> search(SearchProductCriteria criteria, Pageable pageable);
}
