package org.atlas.platform.auth.common.domain.repository;

import java.util.Optional;
import org.atlas.platform.auth.common.domain.entity.User;

public interface UserRepository {

  Optional<User> findById(Integer userId);

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  Optional<User> findByPhoneNumber(String phoneNumber);

  void insert(User user);
}
