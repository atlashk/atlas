package org.atlas.services.inventory.infrastructure.persistence.jpa.adapter;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.exception.BaseDomainException;
import org.atlas.libs.framework.domain.shared.inventory.InsufficientStockException;
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.inventory.infrastructure.persistence.jpa.entity.JpaOptimisticStockEntity;
import org.atlas.services.inventory.infrastructure.persistence.jpa.entity.JpaStockEntity;
import org.atlas.services.inventory.infrastructure.persistence.jpa.mapper.JpaStockMapper;
import org.atlas.services.inventory.infrastructure.persistence.jpa.repository.JpaOptimisticStockRepository;
import org.atlas.services.inventory.infrastructure.persistence.jpa.repository.JpaStockRepository;
import org.atlas.services.inventory.port.out.repository.StockRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaStockRepositoryAdapter implements StockRepository {

  private final JpaStockRepository jpaStockRepository;
  private final JpaOptimisticStockRepository jpaOptimisticStockRepository;

  @Override
  public Optional<StockEntity> findByProductId(String productId) {
    return jpaStockRepository.findByProductId(productId)
        .map(JpaStockMapper.INSTANCE::toStock);
  }

  @Override
  public void insert(StockEntity stock) {
    JpaStockEntity jpaStock = JpaStockMapper.INSTANCE.toJpaStock(stock);
    jpaStockRepository.insert(jpaStock);
  }

  @Override
  public void update(StockEntity stock) {
    JpaStockEntity jpaStock = jpaStockRepository.findByProductId(stock.getProductId())
        .orElseThrow(() -> new BaseDomainException(CommonDomainError.PRODUCT_NOT_FOUND));
    JpaStockMapper.INSTANCE.merge(stock, jpaStock);
    jpaStockRepository.save(jpaStock);
  }

  @Override
  public StockEntity reserveStockWithConstraint(String productId, Integer quantity)
      throws InsufficientStockException {
    int updated = jpaStockRepository.reserveStockWithConstraint(productId, quantity);
    if (updated == 0) {
      throw new InsufficientStockException();
    }

    // Return the updated entity
    return jpaStockRepository.findByProductId(productId)
        .map(JpaStockMapper.INSTANCE::toStock)
        .orElseThrow(() -> new BaseDomainException(CommonDomainError.STOCK_NOT_FOUND));
  }

  // TODO: Implement retry
  @Override
  public StockEntity reserveStockWithPessimisticLock(String productId, Integer quantity)
      throws InsufficientStockException {
    JpaStockEntity jpaStock = jpaStockRepository.findByProductIdWithLock(productId)
        .orElseThrow(() -> new BaseDomainException(CommonDomainError.STOCK_NOT_FOUND));
    if (jpaStock.getAvailableQuantity() < quantity) {
      throw new InsufficientStockException();
    }

    jpaStock.setAvailableQuantity(jpaStock.getAvailableQuantity() - quantity);
    jpaStock.setReservedQuantity(jpaStock.getReservedQuantity() + quantity);
    try {
      JpaStockEntity saved = jpaStockRepository.save(jpaStock);
      return JpaStockMapper.INSTANCE.toStock(saved);
    } catch (DataAccessException e) {
      throw new InsufficientStockException(e);
    }
  }

  // TODO: Implement retry
  @Override
  public StockEntity reserveStockWithOptimisticLock(String productId, Integer quantity)
      throws InsufficientStockException {
    JpaOptimisticStockEntity jpaOptimisticStock =
        jpaOptimisticStockRepository.findById(productId)
            .orElseThrow(() -> new BaseDomainException(CommonDomainError.STOCK_NOT_FOUND));
    if (jpaOptimisticStock.getAvailableQuantity() < quantity) {
      throw new InsufficientStockException();
    }

    jpaOptimisticStock.setAvailableQuantity(jpaOptimisticStock.getAvailableQuantity() - quantity);
    jpaOptimisticStock.setReservedQuantity(jpaOptimisticStock.getReservedQuantity() + quantity);
    try {
      JpaOptimisticStockEntity saved = jpaOptimisticStockRepository.save(jpaOptimisticStock);
      return JpaStockMapper.INSTANCE.toStock(saved);
    } catch (OptimisticLockingFailureException e) {
      throw new InsufficientStockException(e);
    }
  }

  @Override
  public void releaseStock(String productId, Integer quantity) {
    jpaStockRepository.releaseStock(productId, quantity);
  }

  @Override
  public void deleteByProductId(String productId) {
    jpaStockRepository.deleteByProductId(productId);
  }
}
