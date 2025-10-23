package org.atlas.infrastructure.search.elasticsearch.impl.product.repository;

import java.util.Optional;
import org.atlas.infrastructure.search.elasticsearch.impl.product.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticsearchProductRepository extends
    ElasticsearchRepository<ProductDocument, String>, CustomElasticsearchProductRepository {

  Optional<ProductDocument> findByProductId(Integer productId);
}
