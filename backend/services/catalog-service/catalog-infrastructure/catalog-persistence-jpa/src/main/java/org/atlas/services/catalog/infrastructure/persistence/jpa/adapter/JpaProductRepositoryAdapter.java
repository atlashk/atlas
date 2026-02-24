package org.atlas.services.catalog.infrastructure.persistence.jpa.adapter;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.error.DomainError;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaProductEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.mapper.JpaProductMapper;
import org.atlas.services.catalog.infrastructure.persistence.jpa.repository.CustomJpaProductRepository;
import org.atlas.services.catalog.infrastructure.persistence.jpa.repository.JpaProductRepository;
import org.atlas.services.catalog.port.out.repository.ProductRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaProductRepositoryAdapter implements ProductRepository {

  private final JpaProductRepository jpaProductRepository;
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
    List<ProductEntity> products = MapperUtil.mapList(jpaProducts,
        JpaProductMapper.INSTANCE::toProduct);
    return PagingResult.of(products, totalCount, pagingRequest);
  }

  @Override
  public List<ProductEntity> findByIdIn(List<String> ids) {
    List<JpaProductEntity> jpaProducts = jpaProductRepository.findAllByIdInWithAssociations(ids);
    return MapperUtil.mapList(jpaProducts, JpaProductMapper.INSTANCE::toProduct);
  }

  @Override
  public Optional<ProductEntity> findById(String id) {
    return jpaProductRepository.findByIdWithAssociations(id)
        .map(JpaProductMapper.INSTANCE::toProduct);
  }

  @Override
  public Long countAll() {
    return jpaProductRepository.count();
  }

  @Override
  public void insert(ProductEntity product) {
    JpaProductEntity jpaProduct = JpaProductMapper.INSTANCE.toJpaProduct(product);
    jpaProductRepository.insert(jpaProduct);
  }

  @Override
  public void insertAll(List<ProductEntity> products) {
    List<JpaProductEntity> jpaProducts =
        MapperUtil.mapList(products, JpaProductMapper.INSTANCE::toJpaProduct);
    jpaProductRepository.saveAll(jpaProducts);
  }

  @Override
  public void update(ProductEntity product) {
    JpaProductEntity jpaProduct = jpaProductRepository.findByIdWithAssociations(product.getId())
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
    JpaProductMapper.INSTANCE.merge(product, jpaProduct);
    jpaProductRepository.save(jpaProduct);
  }

  @Override
  public void deleteById(String id) {
    jpaProductRepository.deleteById(id);
  }
}
