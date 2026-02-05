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
import org.atlas.libs.framework.domain.product.ProductStockStatus;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.product.domain.entity.ProductEntity;

public interface ProductRepository {

  PagingResult<ProductEntity> findByCriteria(FindProductCriteria criteria,
      PagingRequest pagingRequest);

  Long countAll();

  List<ProductEntity> findByProductIdIn(List<String> productIds);

  Optional<ProductEntity> findByProductId(String productId);

  void insert(ProductEntity product);

  void insertBatch(List<ProductEntity> products);

  void update(ProductEntity product);

  void decreaseQuantityWithConstraint(String productId, Integer decrement)
      throws OutOfStockException;

  void decreaseQuantityWithPessimisticLock(String productId, Integer decrement)
      throws OutOfStockException;

  void decreaseQuantityWithOptimisticLock(String productId, Integer decrement)
      throws OutOfStockException;

  void increaseQuantity(String productId, Integer increment);

  void deleteByProductId(String productId);

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  class FindProductCriteria {

    private String productId;

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
