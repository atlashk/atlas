package org.atlas.infrastructure.fulltextsearch.elasticsearch.impl.product.repository;

import org.atlas.domain.product.infrastructure.fulltextsearch.SearchProductCriteria;
import org.atlas.infrastructure.fulltextsearch.elasticsearch.impl.product.document.ElasticsearchProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface CustomElasticsearchProductRepository {

  SearchHits<ElasticsearchProduct> search(SearchProductCriteria criteria, Pageable pageable);
}
