package org.atlas.domain.product.infrastructure.search;

import java.util.List;
import org.atlas.domain.product.entity.Product;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;

public interface SearchService {

  boolean createIndex(SearchIndex index);

  long countDocuments(SearchIndex index);

  /**
   * @return the IDs of products that match the search criteria
   */
  PagingResult<Integer> search(SearchProductCriteria criteria, PagingRequest pagingRequest);

  void save(Product product);

  void saveAll(List<Product> products);

  void deleteProduct(Integer productId);
}
