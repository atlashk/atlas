package org.atlas.services.product.fulltextsearch.elasticsearch.repository;

import org.atlas.services.product.application.port.fulltextsearch.SearchProductCriteria;
import org.atlas.services.product.fulltextsearch.elasticsearch.document.ElasticsearchProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface CustomElasticsearchProductRepository {

  SearchHits<ElasticsearchProduct> search(SearchProductCriteria criteria, Pageable pageable);
}
