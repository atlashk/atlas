package org.atlas.services.catalog.port.out.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.catalog.domain.entity.Product;
import org.atlas.services.catalog.domain.entity.ProductType;

public interface ProductRepository {

  PagingResult<Product> findByCriteria(FindProductCriteria criteria,
      PagingRequest pagingRequest);

  Long countAll();

  List<Product> findByIdIn(List<String> ids);

  Optional<Product> findById(String id);

  void insert(Product product);

  void insertAll(List<Product> products);

  void update(Product product);

  void deleteById(String id);

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  class FindProductCriteria {

    private String id;

    private String keyword;

    private ProductType type;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private LocalDate startPublishedDate;

    private LocalDate endPublishedDate;

    private Boolean inStock;

    private String brandId;

    private List<String> categoryIds;
  }
}
