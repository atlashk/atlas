package org.atlas.services.iam.infrastructure.persistence.jpa.adapter;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.infrastructure.persistence.jpa.entity.JpaUserEntity;
import org.atlas.services.iam.infrastructure.persistence.jpa.mapper.JpaUserMapper;
import org.atlas.services.iam.infrastructure.persistence.jpa.repository.CustomJpaUserRepository;
import org.atlas.services.iam.infrastructure.persistence.jpa.repository.JpaUserRepository;
import org.atlas.services.iam.port.out.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaUserRepository;
  private final CustomJpaUserRepository customJpaUserRepository;

  @Override
  public PagingResult<UserEntity> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest) {
    long totalCount = customJpaUserRepository.countByCriteria(criteria);
    if (totalCount == 0L) {
      return PagingResult.empty();
    }

    List<JpaUserEntity> jpaUsers = customJpaUserRepository.findByCriteria(criteria, pagingRequest);
    List<UserEntity> users = MapperUtil.mapList(jpaUsers, JpaUserMapper.INSTANCE::toUser);
    return PagingResult.of(users, totalCount, pagingRequest);
  }

  @Override
  public List<UserEntity> findByIdIn(List<String> ids) {
    if (CollectionUtil.isEmpty(ids)) {
      return List.of();
    }
    List<JpaUserEntity> jpaUsers = jpaUserRepository.findAllById(ids);
    return MapperUtil.mapList(jpaUsers, JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<UserEntity> findById(String id) {
    return jpaUserRepository.findById(id)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<UserEntity> findByUsername(String username) {
    return jpaUserRepository.findByUsername(username)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<UserEntity> findByEmail(String email) {
    return jpaUserRepository.findByEmail(email)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<UserEntity> findByPhoneNumber(String phoneNumber) {
    return jpaUserRepository.findByPhoneNumber(phoneNumber)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Long countAll() {
    return jpaUserRepository.count();
  }

  @Override
  public void insert(UserEntity user) {
    JpaUserEntity jpaUser = JpaUserMapper.INSTANCE.toJpaUser(user);
    jpaUserRepository.save(jpaUser);
    user.setId(jpaUser.getId());
  }

  @Override
  public void update(UserEntity user) {
    JpaUserEntity jpaUser = JpaUserMapper.INSTANCE.toJpaUser(user);
    jpaUserRepository.save(jpaUser);
  }

  @Override
  public void deleteById(String id) {
    jpaUserRepository.deleteById(id);
  }
}
