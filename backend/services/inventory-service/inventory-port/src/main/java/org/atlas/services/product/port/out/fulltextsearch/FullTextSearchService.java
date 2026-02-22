package org.atlas.services.product.port.out.fulltextsearch;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.product.domain.entity.ProductEntity;

public interface FullTextSearchService {

  boolean createIndex(SearchIndex index);

  long countDocuments(SearchIndex index);

  /**
   * @return the IDs of products that match the search criteria
   */
  PagingResult<String> search(SearchProductCriteria criteria, PagingRequest pagingRequest);

  void save(ProductEntity product);

  void saveAll(List<ProductEntity> products);

  void deleteProduct(String productId);
}
