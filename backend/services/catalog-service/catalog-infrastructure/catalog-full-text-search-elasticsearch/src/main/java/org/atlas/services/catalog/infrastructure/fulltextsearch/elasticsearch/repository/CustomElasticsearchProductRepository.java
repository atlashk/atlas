package org.atlas.services.catalog.infrastructure.fulltextsearch.elasticsearch.repository;

import org.atlas.services.catalog.infrastructure.fulltextsearch.elasticsearch.document.ElasticsearchProduct;
import org.atlas.services.catalog.port.out.fulltextsearch.SearchProductCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface CustomElasticsearchProductRepository {

  SearchHits<ElasticsearchProduct> search(SearchProductCriteria criteria, Pageable pageable);
}
