package org.atlas.infrastructure.persistence.jpa.impl.product;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.resilience.RetryUtil;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaOptimisticProductEntity;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaProductEntity;
import org.atlas.infrastructure.persistence.jpa.impl.product.mapper.JpaProductEntityMapper;
import org.atlas.infrastructure.persistence.jpa.impl.product.repository.CustomJpaProductRepository;
import org.atlas.infrastructure.persistence.jpa.impl.product.repository.JpaOptimisticProductRepository;
import org.atlas.infrastructure.persistence.jpa.impl.product.repository.JpaProductRepository;
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
    List<JpaProductEntity> jpaProducts = customJpaProductRepository.findByCriteria(criteria,
        pagingRequest);
    List<ProductEntity> products = ObjectMapperUtil.getInstance()
        .mapList(jpaProducts, JpaProductEntityMapper::toProductEntity);
    return PagingResult.of(products, totalCount, pagingRequest);
  }

  @Override
  public List<ProductEntity> findByIdIn(List<Integer> ids) {
    return jpaProductRepository.findAllById(ids)
        .stream()
        .map(JpaProductEntityMapper::toProductEntity)
        .toList();
  }

  @Override
  public Optional<ProductEntity> findById(Integer id) {
    return jpaProductRepository.findByIdWithAssociations(id)
        .map(JpaProductEntityMapper::toProductEntity);
  }

  @Override
  public Long countAll() {
    return jpaProductRepository.count();
  }

  @Override
  public void insert(ProductEntity product) {
    JpaProductEntity jpaProduct = JpaProductEntityMapper.toJpaProductEntity(product);
    jpaProductRepository.insert(jpaProduct);
    product.setId(jpaProduct.getId());
  }

  @Override
  public void insertBatch(List<ProductEntity> products) {
    List<JpaProductEntity> jpaProducts = ObjectMapperUtil.getInstance()
        .mapList(products, JpaProductEntityMapper::toJpaProductEntity);
    jpaProductRepository.saveAll(jpaProducts);
  }

  @Override
  public void update(ProductEntity product) {
    JpaProductEntity jpaProduct = jpaProductRepository.findByIdWithAssociations(product.getId())
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
    JpaProductEntityMapper.merge(product, jpaProduct);
    jpaProductRepository.save(jpaProduct);
  }

  @Override
  public void decreaseQuantityWithConstraint(Integer id, Integer decrement) {
    int updated = jpaProductRepository.decreaseQuantityWithConstraint(id, decrement);
    if (updated == 0) {
      throw new DomainException(DomainError.PRODUCT_INSUFFICIENT_QUANTITY);
    }
  }

  @Override
  public void decreaseQuantityWithPessimisticLock(Integer id, Integer decrement) {
    RetryUtil.retryOn(() -> {
      JpaProductEntity product = jpaProductRepository.findByIdWithLock(id)
          .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
      if (product.getQuantity() < decrement) {
        throw new DomainException(DomainError.PRODUCT_INSUFFICIENT_QUANTITY);
      }
      product.setQuantity(product.getQuantity() - decrement);
      jpaProductRepository.save(product);
    }, DataAccessException.class);
  }

  @Override
  public void decreaseQuantityWithOptimisticLock(Integer id, Integer decrement) {
    RetryUtil.retryOn(() -> {
      JpaOptimisticProductEntity jpaOptimisticProductEntity =
          jpaOptimisticProductRepository.findById(id)
              .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
      if (jpaOptimisticProductEntity.getQuantity() < decrement) {
        throw new DomainException(DomainError.PRODUCT_INSUFFICIENT_QUANTITY);
      }
      jpaOptimisticProductEntity.setQuantity(jpaOptimisticProductEntity.getQuantity() - decrement);
      jpaOptimisticProductRepository.save(jpaOptimisticProductEntity);
    }, OptimisticLockingFailureException.class);
  }

  @Override
  public void increaseQuantity(Integer id, Integer increment) {
    jpaProductRepository.increaseQuantity(id, increment);
  }

  @Override
  public void delete(Integer id) {
    jpaProductRepository.deleteById(id);
  }
}
