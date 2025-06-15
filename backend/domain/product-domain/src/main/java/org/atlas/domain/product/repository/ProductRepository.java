package org.atlas.domain.product.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;

public interface ProductRepository {

  PagingResult<ProductEntity> findByCriteria(FindProductCriteria criteria,
      PagingRequest pagingRequest);

  List<ProductEntity> findByIdIn(List<Integer> ids);

  Optional<ProductEntity> findById(Integer id);

  void insert(ProductEntity productEntity);

  void update(ProductEntity productEntity);

  void decreaseQuantityWithConstraint(Integer id, Integer decrement);

  void decreaseQuantityWithPessimisticLock(Integer id, Integer decrement);

  void decreaseQuantityWithOptimisticLock(Integer id, Integer decrement);

  void insertBatch(List<ProductEntity> productEntities);

  void delete(Integer id);
}
