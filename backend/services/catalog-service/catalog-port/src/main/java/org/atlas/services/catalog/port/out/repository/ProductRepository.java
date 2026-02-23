package org.atlas.services.catalog.port.out.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.services.catalog.domain.entity.ProductType;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.catalog.domain.entity.ProductEntity;

public interface ProductRepository {

  PagingResult<ProductEntity> findByCriteria(FindProductCriteria criteria,
      PagingRequest pagingRequest);

  Long countAll();

  List<ProductEntity> findByIdIn(List<String> ids);

  Optional<ProductEntity> findById(String id);

  void insert(ProductEntity product);

  void insertBatch(List<ProductEntity> products);

  void update(ProductEntity product);

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

    private Date startPublishedAt;

    private Date endPublishedAt;

    private Boolean inStock;

    private String brandId;

    private List<String> categoryIds;
  }
}
