package org.atlas.infrastructure.persistence.jpa.adapter.auth;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.entity.UserEntity;
import org.atlas.domain.auth.repository.UserRepository;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.adapter.auth.entity.JpaUserEntity;
import org.atlas.infrastructure.persistence.jpa.adapter.auth.repository.JpaAuthUserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

  private final JpaAuthUserRepository jpaAuthUserRepository;

  @Override
  public Optional<UserEntity> findById(Integer id) {
    return Optional.empty();
  }

  @Override
  public Optional<UserEntity> findByIdentifier(String identifier) {
    return jpaAuthUserRepository.findByIdentifier(identifier)
        .map(jpaUserEntity -> ObjectMapperUtil.getInstance()
            .map(jpaUserEntity, UserEntity.class));
  }

  @Override
  public void insert(UserEntity userEntity) {
    JpaUserEntity jpaUserEntity = ObjectMapperUtil.getInstance()
        .map(userEntity, JpaUserEntity.class);
    jpaAuthUserRepository.save(jpaUserEntity);
  }
}
