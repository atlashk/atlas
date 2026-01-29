package org.atlas.platform.auth.common.persistence.jpa.adapter;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.platform.auth.common.domain.entity.User;
import org.atlas.platform.auth.common.domain.repository.UserRepository;
import org.atlas.platform.auth.common.persistence.jpa.entity.JpaUser;
import org.atlas.platform.auth.common.persistence.jpa.mapper.JpaUserMapper;
import org.atlas.platform.auth.common.persistence.jpa.repository.JpaUserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaUserRepository;

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
  public void insert(User user) {
    JpaUser jpaUser = JpaUserMapper.INSTANCE.toJpaUser(user);
    jpaUserRepository.save(jpaUser);
  }
}
