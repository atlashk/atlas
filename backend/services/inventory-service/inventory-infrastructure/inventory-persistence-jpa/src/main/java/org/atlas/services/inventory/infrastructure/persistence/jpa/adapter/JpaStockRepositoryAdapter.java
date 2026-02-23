package org.atlas.services.inventory.infrastructure.persistence.jpa.adapter;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.domain.common.exception.OutOfStockException;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.inventory.infrastructure.persistence.jpa.entity.JpaProductEntity;
import org.atlas.services.product.port.out.repository.StockRepository;
import org.atlas.services.inventory.infrastructure.persistence.jpa.entity.JpaOptimisticProductEntity;
import org.atlas.services.inventory.infrastructure.persistence.jpa.mapper.JpaProductMapper;
import org.atlas.services.inventory.infrastructure.persistence.jpa.repository.CustomJpaProductRepository;
import org.atlas.services.inventory.infrastructure.persistence.jpa.repository.JpaOptimisticProductRepository;
import org.atlas.services.inventory.infrastructure.persistence.jpa.repository.JpaProductRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaStockRepositoryAdapter implements StockRepository {

  private final JpaProductRepository jpaProductRepository;
  private final JpaOptimisticProductRepository jpaOptimisticProductRepository;
  private final CustomJpaProductRepository customJpaProductRepository;

  @Override
  public PagingResult<StockEntity> findByCriteria(FindProductCriteria criteria,
      PagingRequest pagingRequest) {
    long totalCount = customJpaProductRepository.countByCriteria(criteria);
    if (totalCount == 0L) {
      return PagingResult.empty();
    }
    List<JpaProductEntity> jpaProducts = customJpaProductRepository.findByCriteria(criteria,
        pagingRequest);
    List<StockEntity> products = MapperUtil.mapList(jpaProducts,
        JpaProductMapper.INSTANCE::toProduct);
    return PagingResult.of(products, totalCount, pagingRequest);
  }

  @Override
  public List<StockEntity> findByIdIn(List<String> ids) {
    List<JpaProductEntity> jpaProducts = jpaProductRepository.findAllByIdInWithAssociations(ids);
    return MapperUtil.mapList(jpaProducts, JpaProductMapper.INSTANCE::toProduct);
  }

  @Override
  public Optional<StockEntity> findById(String id) {
    return jpaProductRepository.findByIdWithAssociations(id)
        .map(JpaProductMapper.INSTANCE::toProduct);
  }

  @Override
  public Long countAll() {
    return jpaProductRepository.count();
  }

  @Override
  public void insert(StockEntity product) {
    JpaProductEntity jpaProduct = JpaProductMapper.INSTANCE.toJpaProduct(product);
    jpaProductRepository.insert(jpaProduct);
  }

  @Override
  public void insertBatch(List<StockEntity> products) {
    List<JpaProductEntity> jpaProducts =
        MapperUtil.mapList(products, JpaProductMapper.INSTANCE::toJpaProduct);
    jpaProductRepository.saveAll(jpaProducts);
  }

  @Override
  public void update(StockEntity product) {
    JpaProductEntity jpaProduct = jpaProductRepository.findByIdWithAssociations(product.getId())
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
    JpaProductMapper.INSTANCE.merge(product, jpaProduct);
    jpaProductRepository.save(jpaProduct);
  }

  @Override
  public void decreaseQuantityWithConstraint(String id, Integer decrement)
      throws OutOfStockException {
    int updated = jpaProductRepository.decreaseQuantityWithConstraint(id, decrement);
    if (updated == 0) {
      throw new OutOfStockException();
    }
  }

  // TODO: Implement retry
  @Override
  public void decreaseQuantityWithPessimisticLock(String id, Integer decrement)
      throws OutOfStockException {
    JpaProductEntity product = jpaProductRepository.findByIdWithLock(id)
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
    if (product.getQuantity() < decrement) {
      throw new OutOfStockException();
    }

    product.setQuantity(product.getQuantity() - decrement);
    try {
      jpaProductRepository.save(product);
    } catch (DataAccessException e) {
      throw new OutOfStockException(e);
    }
  }

  // TODO: Implement retry
  @Override
  public void decreaseQuantityWithOptimisticLock(String id, Integer decrement)
      throws OutOfStockException {
    JpaOptimisticProductEntity jpaOptimisticProduct =
        jpaOptimisticProductRepository.findById(id)
            .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
    if (jpaOptimisticProduct.getQuantity() < decrement) {
      throw new OutOfStockException();
    }

    jpaOptimisticProduct.setQuantity(jpaOptimisticProduct.getQuantity() - decrement);
    try {
      jpaOptimisticProductRepository.save(jpaOptimisticProduct);
    } catch (OptimisticLockingFailureException e) {
      throw new OutOfStockException(e);
    }
  }

  @Override
  public void increaseQuantity(String id, Integer increment) {
    jpaProductRepository.increaseQuantity(id, increment);
  }

  @Override
  public void deleteById(String id) {
    jpaProductRepository.deleteById(id);
  }
}
