package org.atlas.infrastructure.fulltextsearch.elasticsearch.adapter.product.repository;

import org.atlas.application.product.port.fulltextsearch.SearchProductCriteria;
import org.atlas.infrastructure.fulltextsearch.elasticsearch.adapter.product.document.ElasticsearchProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface CustomElasticsearchProductRepository {

  SearchHits<ElasticsearchProduct> search(SearchProductCriteria criteria, Pageable pageable);
}
