package org.atlas.services.catalog.infrastructure.search.elasticsearch.repository;

import org.atlas.services.catalog.infrastructure.search.elasticsearch.document.EsProduct;
import org.atlas.services.catalog.port.out.search.ProductSearchService.SearchProductCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface CustomEsProductRepository {

  SearchHits<EsProduct> search(SearchProductCriteria criteria, Pageable pageable);
}
