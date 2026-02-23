package org.atlas.services.product.port.out.repository;

import org.atlas.libs.framework.domain.common.exception.OutOfStockException;
import org.atlas.services.inventory.domain.entity.StockEntity;

public interface StockRepository {

  void insert(StockEntity stock);

  void decreaseQuantityWithConstraint(String productId, Integer decrement)
      throws OutOfStockException;

  void decreaseQuantityWithPessimisticLock(String productId, Integer decrement)
      throws OutOfStockException;

  void decreaseQuantityWithOptimisticLock(String productId, Integer decrement)
      throws OutOfStockException;

  void increaseQuantity(String productId, Integer increment);

  void deleteByProductId(String productId);
}
