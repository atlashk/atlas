package org.atlas.services.catalog.infrastructure.search.elasticsearch.repository;

import java.util.Optional;
import org.atlas.services.catalog.infrastructure.search.elasticsearch.document.EsProduct;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EsProductRepository extends
    ElasticsearchRepository<EsProduct, String>, CustomEsProductRepository {

  Optional<EsProduct> findByProductId(String productId);
}
