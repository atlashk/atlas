package org.atlas.services.product.port.out.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.common.exception.OutOfStockException;
import org.atlas.libs.framework.domain.catalog.ProductStockStatus;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.product.domain.entity.ProductEntity;

public interface ProductRepository {

  PagingResult<ProductEntity> findByCriteria(FindProductCriteria criteria,
      PagingRequest pagingRequest);

  Long countAll();

  List<ProductEntity> findByIdIn(List<String> ids);

  Optional<ProductEntity> findById(String id);

  void insert(ProductEntity product);

  void insertBatch(List<ProductEntity> products);

  void update(ProductEntity product);

  void decreaseQuantityWithConstraint(String id, Integer decrement)
      throws OutOfStockException;

  void decreaseQuantityWithPessimisticLock(String id, Integer decrement)
      throws OutOfStockException;

  void decreaseQuantityWithOptimisticLock(String id, Integer decrement)
      throws OutOfStockException;

  void increaseQuantity(String id, Integer increment);

  void deleteById(String id);

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  class FindProductCriteria {

    private String id;

    private String keyword;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private ProductStockStatus stockStatus;

    private Date availableFrom;

    private Boolean isActive;

    private Integer brandId;

    private List<Integer> categoryIds;
  }
}
