package org.atlas.infrastructure.persistence.jpa.adapter.auth;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.entity.User;
import org.atlas.domain.auth.repository.UserRepository;
import org.atlas.infrastructure.persistence.jpa.adapter.auth.entity.JpaUser;
import org.atlas.infrastructure.persistence.jpa.adapter.auth.mapper.JpaUserMapper;
import org.atlas.infrastructure.persistence.jpa.adapter.auth.repository.JpaUserRepository;
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
