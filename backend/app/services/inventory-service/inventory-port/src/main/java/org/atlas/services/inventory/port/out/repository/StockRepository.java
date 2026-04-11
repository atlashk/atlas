package org.atlas.services.inventory.port.out.repository;

import java.util.Optional;
import org.atlas.libs.framework.domain.shared.inventory.InsufficientStockException;
import org.atlas.services.inventory.domain.entity.Stock;

public interface StockRepository {

  Optional<Stock> findByProductId(String productId);

  void insert(Stock stock);

  void update(Stock stock);

  Stock reserveStockWithConstraint(String productId, Integer decrement)
      throws InsufficientStockException;

  Stock reserveStockWithPessimisticLock(String productId, Integer decrement)
      throws InsufficientStockException;

  Stock reserveStockWithOptimisticLock(String productId, Integer decrement)
      throws InsufficientStockException;

  void releaseStock(String productId, Integer increment);
}
