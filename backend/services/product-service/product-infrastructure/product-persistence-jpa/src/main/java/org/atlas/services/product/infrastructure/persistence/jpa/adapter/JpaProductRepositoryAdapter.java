package org.atlas.services.product.infrastructure.persistence.jpa.adapter;

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
import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.port.out.repository.ProductRepository;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaOptimisticProductEntity;
import org.atlas.services.product.infrastructure.persistence.jpa.entity.JpaProduct;
import org.atlas.services.product.infrastructure.persistence.jpa.mapper.JpaProductMapper;
import org.atlas.services.product.infrastructure.persistence.jpa.repository.CustomJpaProductRepository;
import org.atlas.services.product.infrastructure.persistence.jpa.repository.JpaOptimisticProductRepository;
import org.atlas.services.product.infrastructure.persistence.jpa.repository.JpaProductRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaProductRepositoryAdapter implements ProductRepository {

  private final JpaProductRepository jpaProductRepository;
  private final JpaOptimisticProductRepository jpaOptimisticProductRepository;
  private final CustomJpaProductRepository customJpaProductRepository;

  @Override
  public PagingResult<ProductEntity> findByCriteria(FindProductCriteria criteria,
      PagingRequest pagingRequest) {
    long totalCount = customJpaProductRepository.countByCriteria(criteria);
    if (totalCount == 0L) {
      return PagingResult.empty();
    }
    List<JpaProduct> jpaProducts = customJpaProductRepository.findByCriteria(criteria,
        pagingRequest);
    List<ProductEntity> products = MapperUtil.mapList(jpaProducts,
        JpaProductMapper.INSTANCE::toProduct);
    return PagingResult.of(products, totalCount, pagingRequest);
  }

  @Override
  public List<ProductEntity> findByProductIdIn(List<String> productIds) {
    List<JpaProduct> jpaProducts = jpaProductRepository.findAllByProductIdInWithAssociations(productIds);
    return MapperUtil.mapList(jpaProducts, JpaProductMapper.INSTANCE::toProduct);
  }

  @Override
  public Optional<ProductEntity> findByProductId(String productId) {
    return jpaProductRepository.findByProductIdWithAssociations(productId)
        .map(JpaProductMapper.INSTANCE::toProduct);
  }

  @Override
  public Long countAll() {
    return jpaProductRepository.count();
  }

  @Override
  public void insert(ProductEntity product) {
    JpaProduct jpaProduct = JpaProductMapper.INSTANCE.toJpaProduct(product);
    jpaProductRepository.insert(jpaProduct);
    product.setProductId(jpaProduct.getProductId());
  }

  @Override
  public void insertBatch(List<ProductEntity> products) {
    List<JpaProduct> jpaProducts =
        MapperUtil.mapList(products, JpaProductMapper.INSTANCE::toJpaProduct);
    jpaProductRepository.saveAll(jpaProducts);
  }

  @Override
  public void update(ProductEntity product) {
    JpaProduct jpaProduct = jpaProductRepository.findByProductIdWithAssociations(product.getProductId())
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
    JpaProductMapper.INSTANCE.merge(product, jpaProduct);
    jpaProductRepository.save(jpaProduct);
  }

  @Override
  public void decreaseQuantityWithConstraint(String productId, Integer decrement)
      throws OutOfStockException {
    int updated = jpaProductRepository.decreaseQuantityWithConstraint(productId, decrement);
    if (updated == 0) {
      throw new OutOfStockException();
    }
  }

  // TODO: Implement retry
  @Override
  public void decreaseQuantityWithPessimisticLock(String productId, Integer decrement)
      throws OutOfStockException {
    JpaProduct product = jpaProductRepository.findByProductIdWithLock(productId)
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
  public void decreaseQuantityWithOptimisticLock(String productId, Integer decrement)
      throws OutOfStockException {
    JpaOptimisticProductEntity jpaOptimisticProduct =
        jpaOptimisticProductRepository.findById(productId)
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
  public void increaseQuantity(String productId, Integer increment) {
    jpaProductRepository.increaseQuantity(productId, increment);
  }

  @Override
  public void deleteByProductId(String productId) {
    jpaProductRepository.deleteById(productId);
  }
}
