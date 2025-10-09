package org.atlas.domain.product.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;

public interface ProductRepository {

  PagingResult<ProductEntity> findByCriteria(FindProductCriteria criteria,
      PagingRequest pagingRequest);

  Long countAll();

  List<ProductEntity> findByIdIn(List<Integer> ids);

  Optional<ProductEntity> findById(Integer id);

  void insert(ProductEntity product);

  void insertBatch(List<ProductEntity> products);

  void update(ProductEntity product);

  void decreaseQuantityWithConstraint(Integer id, Integer decrement);

  void decreaseQuantityWithPessimisticLock(Integer id, Integer decrement);

  void decreaseQuantityWithOptimisticLock(Integer id, Integer decrement);

  void increaseQuantity(Integer id, Integer increment);

  void delete(Integer id);
}
