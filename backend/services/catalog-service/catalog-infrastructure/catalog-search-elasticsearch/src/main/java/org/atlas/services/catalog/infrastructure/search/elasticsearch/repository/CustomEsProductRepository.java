package org.atlas.services.catalog.infrastructure.search.elasticsearch.repository;

import org.atlas.services.catalog.infrastructure.search.elasticsearch.document.EsProduct;
import org.atlas.services.catalog.port.out.search.ProductSearchService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface CustomEsProductRepository {

  SearchHits<EsProduct> search(ProductSearchService.SearchCriteria criteria, Pageable pageable);
}
