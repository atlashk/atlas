package org.atlas.services.product.infrastructure.fulltextsearch.elasticsearch.repository;

import java.util.Optional;
import org.atlas.services.product.infrastructure.fulltextsearch.elasticsearch.document.ElasticsearchProduct;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticsearchProductRepository extends
    ElasticsearchRepository<ElasticsearchProduct, String>, CustomElasticsearchProductRepository {

  Optional<ElasticsearchProduct> findByProductId(String productId);
}
