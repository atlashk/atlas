package org.atlas.application.product.port.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.application.product.port.repository.criteria.FindProductCriteria;
import org.atlas.domain.product.entity.Product;
import org.atlas.framework.domain.exception.OutOfStockException;
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

  void decreaseQuantityWithConstraint(Integer id, Integer decrement) throws OutOfStockException;

  void decreaseQuantityWithPessimisticLock(Integer id, Integer decrement)
      throws OutOfStockException;

  void decreaseQuantityWithOptimisticLock(Integer id, Integer decrement) throws OutOfStockException;

  void increaseQuantity(Integer id, Integer increment);

  void delete(Integer id);
}
