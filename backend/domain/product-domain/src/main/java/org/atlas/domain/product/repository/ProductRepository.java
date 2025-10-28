package org.atlas.domain.product.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;

public interface ProductRepository {

  PagingResult<Product> findByCriteria(FindProductCriteria criteria,
      PagingRequest pagingRequest);

  Long countAll();

  List<Product> findByIdIn(List<Integer> ids);

  Optional<Product> findById(Integer id);

  void insert(Product product);

  void insertBatch(List<Product> products);

  void update(Product product);

  void decreaseQuantityWithConstraint(Integer id, Integer decrement);

  void decreaseQuantityWithPessimisticLock(Integer id, Integer decrement);

  void decreaseQuantityWithOptimisticLock(Integer id, Integer decrement);

  void increaseQuantity(Integer id, Integer increment);

  void delete(Integer id);
}
