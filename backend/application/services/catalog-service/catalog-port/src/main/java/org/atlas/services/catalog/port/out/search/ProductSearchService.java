package org.atlas.services.catalog.port.out.search;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.catalog.domain.entity.ProductEntity;

public interface ProductSearchService {

  boolean createIndex();

  long countDocuments();

  /**
   * @return the IDs of products that match the search criteria
   */
  PagingResult<String> search(SearchProductCriteria criteria, PagingRequest pagingRequest);

  void save(ProductEntity product);

  void saveAll(List<ProductEntity> products);

  void delete(String productId);

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  class SearchProductCriteria {

    private String keyword;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private String brandId;

    private List<String> categoryIds;
  }
}
