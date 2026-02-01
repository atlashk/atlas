package org.atlas.services.product.port.out.fulltextsearch;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.product.domain.entity.Product;

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
