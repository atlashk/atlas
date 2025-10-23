package org.atlas.infrastructure.search.elasticsearch.impl.product.repository;

import org.atlas.domain.product.infrastructure.search.SearchProductCriteria;
import org.atlas.infrastructure.search.elasticsearch.impl.product.document.ProductDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface CustomElasticsearchProductRepository {

  SearchHits<ProductDocument> search(SearchProductCriteria criteria, Pageable pageable);
}
