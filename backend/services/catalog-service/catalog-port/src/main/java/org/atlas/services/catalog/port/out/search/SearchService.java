package org.atlas.services.catalog.port.out.search;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.catalog.domain.entity.ProductEntity;

public interface SearchService {

  boolean createIndex(SearchIndex index);

  long countDocuments(SearchIndex index);

  /**
   * @return the IDs of products that match the search criteria
   */
  PagingResult<String> search(SearchProductCriteria criteria, PagingRequest pagingRequest);

  void save(ProductEntity product);

  void saveAll(List<ProductEntity> products);

  void delete(String productId);
}
