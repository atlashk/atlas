package org.atlas.services.inventory.port.out.repository;

import java.util.Optional;
import org.atlas.libs.framework.domain.shared.inventory.InsufficientStockException;
import org.atlas.services.inventory.domain.entity.StockEntity;

public interface StockRepository {

  Optional<StockEntity> findByProductId(String productId);

  void insert(StockEntity stock);

  void update(StockEntity stock);

  StockEntity reserveStockWithConstraint(String productId, Integer decrement)
      throws InsufficientStockException;

  StockEntity reserveStockWithPessimisticLock(String productId, Integer decrement)
      throws InsufficientStockException;

  StockEntity reserveStockWithOptimisticLock(String productId, Integer decrement)
      throws InsufficientStockException;

  void releaseStock(String productId, Integer increment);
}
