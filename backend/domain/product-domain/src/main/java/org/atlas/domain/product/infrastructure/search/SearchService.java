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

  /**
   * Synchronize a batch of products to the search engine
   *
   * @param products List of products to synchronize
   */
  void saveAll(List<Product> products);

  /**
   * Delete a product from the search engine
   *
   * @param productId ID of the product to delete
   */
  void deleteProduct(Integer productId);
}
