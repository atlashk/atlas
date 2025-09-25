package org.atlas.infrastructure.persistence.jpa.impl.user;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaCartEntity;
import org.atlas.infrastructure.persistence.jpa.impl.user.mapper.JpaCartEntityMapper;
import org.atlas.infrastructure.persistence.jpa.impl.user.repository.JpaCartRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaCartRepositoryAdapter implements CartRepository {

  private final JpaCartRepository jpaCartRepository;

  @Override
  public Optional<CartEntity> findByUserId(Integer userId) {
    return jpaCartRepository.findByUserIdAndFetch(userId)
        .map(JpaCartEntityMapper::toCartEntity);
  }

  @Override
  public void insert(CartEntity cartEntity) {
    JpaCartEntity jpaCartEntity = JpaCartEntityMapper.toJpaCartEntity(cartEntity);
    jpaCartRepository.insert(jpaCartEntity);
    cartEntity.setId(jpaCartEntity.getId());
  }

  @Override
  public void update(CartEntity cartEntity) {
    JpaCartEntity jpaCartEntity = JpaCartEntityMapper.toJpaCartEntity(cartEntity);
    jpaCartRepository.save(jpaCartEntity);
  }
}
