package org.atlas.services.product.port.out.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.libs.framework.domain.common.exception.OutOfStockException;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.port.out.repository.criteria.FindProductCriteria;

public interface ProductRepository {

  PagingResult<ProductEntity> findByCriteria(FindProductCriteria criteria,
      PagingRequest pagingRequest);

  Long countAll();

  List<ProductEntity> findByProductIdIn(List<String> productIds);

  Optional<ProductEntity> findByProductId(String productId);

  void insert(ProductEntity product);

  void insertBatch(List<ProductEntity> products);

  void update(ProductEntity product);

  void decreaseQuantityWithConstraint(String productId, Integer decrement) throws OutOfStockException;

  void decreaseQuantityWithPessimisticLock(String productId, Integer decrement)
      throws OutOfStockException;

  void decreaseQuantityWithOptimisticLock(String productId, Integer decrement) throws OutOfStockException;

  void increaseQuantity(String productId, Integer increment);

  void deleteByProductId(String productId);
}
