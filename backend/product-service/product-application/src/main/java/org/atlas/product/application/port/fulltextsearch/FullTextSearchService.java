package org.atlas.product.application.port.fulltextsearch;

import java.util.List;
import org.atlas.product.domain.entity.Product;
import org.atlas.common.framework.paging.PagingRequest;
import org.atlas.common.framework.paging.PagingResult;

public interface FullTextSearchService {

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
