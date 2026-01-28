package org.atlas.product.persistence.jpa.adapter;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.product.application.port.repository.ProductRepository;
import org.atlas.product.application.port.repository.criteria.FindProductCriteria;
import org.atlas.product.domain.entity.Product;
import org.atlas.common.framework.domain.common.error.DomainError;
import org.atlas.common.framework.domain.common.exception.DomainException;
import org.atlas.common.framework.domain.common.exception.OutOfStockException;
import org.atlas.common.framework.paging.PagingRequest;
import org.atlas.common.framework.paging.PagingResult;
import org.atlas.common.framework.util.ObjectMapperUtil;
import org.atlas.product.persistence.jpa.entity.JpaOptimisticProduct;
import org.atlas.product.persistence.jpa.entity.JpaProduct;
import org.atlas.product.persistence.jpa.mapper.JpaProductMapper;
import org.atlas.product.persistence.jpa.repository.CustomJpaProductRepository;
import org.atlas.product.persistence.jpa.repository.JpaOptimisticProductRepository;
import org.atlas.product.persistence.jpa.repository.JpaProductRepository;
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
  public PagingResult<Product> findByCriteria(FindProductCriteria criteria,
      PagingRequest pagingRequest) {
    long totalCount = customJpaProductRepository.countByCriteria(criteria);
    if (totalCount == 0L) {
      return PagingResult.empty();
    }
    List<JpaProduct> jpaProducts = customJpaProductRepository.findByCriteria(criteria,
        pagingRequest);
    List<Product> products = ObjectMapperUtil.mapList(jpaProducts,
        JpaProductMapper.INSTANCE::toProduct);
    return PagingResult.of(products, totalCount, pagingRequest);
  }

  @Override
  public List<Product> findByIdIn(List<Integer> ids) {
    List<JpaProduct> jpaProducts = jpaProductRepository.findAllByIdInWithAssociations(ids);
    return ObjectMapperUtil.mapList(jpaProducts, JpaProductMapper.INSTANCE::toProduct);
  }

  @Override
  public Optional<Product> findById(Integer id) {
    return jpaProductRepository.findByIdWithAssociations(id)
        .map(JpaProductMapper.INSTANCE::toProduct);
  }

  @Override
  public Long countAll() {
    return jpaProductRepository.count();
  }

  @Override
  public void insert(Product product) {
    JpaProduct jpaProduct = JpaProductMapper.INSTANCE.toJpaProduct(product);
    jpaProductRepository.insert(jpaProduct);
    product.setId(jpaProduct.getId());
  }

  @Override
  public void insertBatch(List<Product> products) {
    List<JpaProduct> jpaProducts =
        ObjectMapperUtil.mapList(products, JpaProductMapper.INSTANCE::toJpaProduct);
    jpaProductRepository.saveAll(jpaProducts);
  }

  @Override
  public void update(Product product) {
    JpaProduct jpaProduct = jpaProductRepository.findByIdWithAssociations(product.getId())
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
    JpaProductMapper.INSTANCE.merge(product, jpaProduct);
    jpaProductRepository.save(jpaProduct);
  }

  @Override
  public void decreaseQuantityWithConstraint(Integer id, Integer decrement)
      throws OutOfStockException {
    int updated = jpaProductRepository.decreaseQuantityWithConstraint(id, decrement);
    if (updated == 0) {
      throw new OutOfStockException();
    }
  }

  // TODO: Implement retry
  @Override
  public void decreaseQuantityWithPessimisticLock(Integer id, Integer decrement)
      throws OutOfStockException {
    JpaProduct product = jpaProductRepository.findByIdWithLock(id)
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
  public void decreaseQuantityWithOptimisticLock(Integer id, Integer decrement)
      throws OutOfStockException {
    JpaOptimisticProduct jpaOptimisticProduct =
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
  public void increaseQuantity(Integer id, Integer increment) {
    jpaProductRepository.increaseQuantity(id, increment);
  }

  @Override
  public void delete(Integer id) {
    jpaProductRepository.deleteById(id);
  }
}
