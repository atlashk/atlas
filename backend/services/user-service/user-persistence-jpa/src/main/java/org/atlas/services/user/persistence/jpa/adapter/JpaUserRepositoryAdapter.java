package org.atlas.services.user.persistence.jpa.adapter;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.user.application.port.repository.UserRepository;
import org.atlas.services.user.application.port.repository.criteria.FindUserCriteria;
import org.atlas.services.user.domain.entity.User;
import org.atlas.services.user.persistence.jpa.entity.JpaUser;
import org.atlas.services.user.persistence.jpa.mapper.JpaUserMapper;
import org.atlas.services.user.persistence.jpa.repository.CustomJpaUserRepository;
import org.atlas.services.user.persistence.jpa.repository.JpaUserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaUserRepository;
  private final CustomJpaUserRepository customJpaUserRepository;

  @Override
  public PagingResult<User> findByCriteria(FindUserCriteria criteria,
      PagingRequest pagingRequest) {
    long totalCount = customJpaUserRepository.countByCriteria(criteria);
    if (totalCount == 0L) {
      return PagingResult.empty();
    }
    List<JpaUser> jpaUsers = customJpaUserRepository.findByCriteria(criteria, pagingRequest);
    List<User> users = ObjectMapperUtil.mapList(jpaUsers, JpaUserMapper.INSTANCE::toUser);
    return PagingResult.of(users, totalCount, pagingRequest);
  }

  @Override
  public List<User> findByIdIn(List<Integer> ids) {
    List<JpaUser> jpaUsers = jpaUserRepository.findAllById(ids);
    return ObjectMapperUtil.mapList(jpaUsers, JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<User> findById(Integer id) {
    return jpaUserRepository.findById(id)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return jpaUserRepository.findByUsername(username)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaUserRepository.findByEmail(email)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<User> findByPhoneNumber(String phoneNumber) {
    return jpaUserRepository.findByPhoneNumber(phoneNumber)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Long countAll() {
    return jpaUserRepository.count();
  }

  @Override
  public void insert(User user) {
    JpaUser jpaUser = JpaUserMapper.INSTANCE.toJpaUser(user);
    jpaUserRepository.save(jpaUser);
    user.setId(jpaUser.getId());
  }
}
