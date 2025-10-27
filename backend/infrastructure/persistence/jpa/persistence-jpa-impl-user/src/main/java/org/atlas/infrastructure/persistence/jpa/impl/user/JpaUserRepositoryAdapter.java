package org.atlas.infrastructure.persistence.jpa.impl.user;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.domain.user.repository.criteria.FindUserCriteria;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaUserEntity;
import org.atlas.infrastructure.persistence.jpa.impl.user.repository.CustomJpaUserRepository;
import org.atlas.infrastructure.persistence.jpa.impl.user.repository.JpaUserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaUserRepository;
  private final CustomJpaUserRepository customJpaUserRepository;

  @Override
  public PagingResult<UserEntity> findByCriteria(FindUserCriteria criteria,
      PagingRequest pagingRequest) {
    long totalCount = customJpaUserRepository.countByCriteria(criteria);
    if (totalCount == 0L) {
      return PagingResult.empty();
    }
    List<JpaUserEntity> jpaUserEntities = customJpaUserRepository.findByCriteria(criteria,
        pagingRequest);
    List<UserEntity> users = ObjectMapperUtil.getInstance()
        .mapList(jpaUserEntities, UserEntity.class);
    return PagingResult.of(users, totalCount, pagingRequest);
  }

  @Override
  public List<UserEntity> findByIdIn(List<Integer> ids) {
    List<JpaUserEntity> jpaUserEntities = jpaUserRepository.findAllById(ids);
    return ObjectMapperUtil.getInstance()
        .mapList(jpaUserEntities, UserEntity.class);
  }

  @Override
  public Optional<UserEntity> findById(Integer id) {
    return jpaUserRepository.findById(id)
        .map(jpaUser -> ObjectMapperUtil.getInstance()
            .map(jpaUser, UserEntity.class));
  }

  @Override
  public Optional<UserEntity> findByUsername(String username) {
    return jpaUserRepository.findByUsername(username)
        .map(jpaUser -> ObjectMapperUtil.getInstance()
            .map(jpaUser, UserEntity.class));
  }

  @Override
  public Optional<UserEntity> findByEmail(String email) {
    return jpaUserRepository.findByEmail(email)
        .map(jpaUser -> ObjectMapperUtil.getInstance()
            .map(jpaUser, UserEntity.class));
  }

  @Override
  public Optional<UserEntity> findByPhoneNumber(String phoneNumber) {
    return jpaUserRepository.findByPhoneNumber(phoneNumber)
        .map(jpaUser -> ObjectMapperUtil.getInstance()
            .map(jpaUser, UserEntity.class));
  }

  @Override
  public Long countAll() {
    return jpaUserRepository.count();
  }

  @Override
  public void insert(UserEntity user) {
    JpaUserEntity jpaUser = ObjectMapperUtil.getInstance()
        .map(user, JpaUserEntity.class);
    jpaUserRepository.save(jpaUser);
    user.setId(jpaUser.getId());
  }
}
