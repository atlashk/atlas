package org.atlas.infrastructure.fulltextsearch.elasticsearch.impl.product.repository;

import java.util.Optional;
import org.atlas.infrastructure.fulltextsearch.elasticsearch.impl.product.document.ElasticsearchProduct;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticsearchProductRepository extends
    ElasticsearchRepository<ElasticsearchProduct, String>, CustomElasticsearchProductRepository {

  Optional<ElasticsearchProduct> findByProductId(Integer productId);
}
