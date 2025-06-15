package org.atlas.infrastructure.persistence.jpa.adapter.user;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.repository.FindUserCriteria;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.infrastructure.persistence.jpa.adapter.user.entity.JpaUserEntity;
import org.atlas.infrastructure.persistence.jpa.adapter.user.repository.JpaUserRepository;
import org.atlas.infrastructure.persistence.jpa.core.paging.PagingConverter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaUserRepository;

  @Override
  public PagingResult<UserEntity> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest) {
    Pageable pageable = PagingConverter.convert(pagingRequest);
    PagingResult<JpaUserEntity> jpaUserPage = PagingConverter.convert(
        jpaUserRepository.findByCriteria(criteria, pageable));
    return ObjectMapperUtil.getInstance()
        .mapPage(jpaUserPage, UserEntity.class);
  }

  @Override
  public List<UserEntity> findByIdIn(List<Integer> ids) {
    return jpaUserRepository.findAllById(ids)
        .stream()
        .map(jpaUserEntity ->
            ObjectMapperUtil.getInstance().map(jpaUserEntity, UserEntity.class))
        .toList();
  }

  @Override
  public Optional<UserEntity> findById(Integer id) {
    return jpaUserRepository.findById(id)
        .map(jpaUserEntity ->
            ObjectMapperUtil.getInstance().map(jpaUserEntity, UserEntity.class));
  }

  @Override
  public Optional<UserEntity> findByUsername(String username) {
    return jpaUserRepository.findByUsername(username)
        .map(jpaUserEntity ->
            ObjectMapperUtil.getInstance().map(jpaUserEntity, UserEntity.class));
  }

  @Override
  public Optional<UserEntity> findByEmail(String email) {
    return jpaUserRepository.findByEmail(email)
        .map(jpaUserEntity ->
            ObjectMapperUtil.getInstance().map(jpaUserEntity, UserEntity.class));
  }

  @Override
  public Optional<UserEntity> findByPhoneNumber(String phoneNumber) {
    return jpaUserRepository.findByPhoneNumber(phoneNumber)
        .map(jpaUserEntity ->
            ObjectMapperUtil.getInstance().map(jpaUserEntity, UserEntity.class));
  }

  @Override
  public void insert(UserEntity userEntity) {
    JpaUserEntity jpaUserEntity = ObjectMapperUtil.getInstance()
        .map(userEntity, JpaUserEntity.class);
    jpaUserRepository.save(jpaUserEntity);
    userEntity.setId(jpaUserEntity.getId());
  }
}
