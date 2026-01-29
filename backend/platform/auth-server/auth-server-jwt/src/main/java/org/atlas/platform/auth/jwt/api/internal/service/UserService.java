package org.atlas.platform.auth.jwt.api.internal.service;

import lombok.RequiredArgsConstructor;
import org.atlas.platform.auth.common.domain.entity.User;
import org.atlas.platform.auth.common.domain.repository.UserRepository;
import org.atlas.platform.auth.jwt.api.internal.mapper.UserMapper;
import org.atlas.platform.auth.jwt.api.internal.model.CreateUserRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void createUser(CreateUserRequest request) {
    User user = UserMapper.INSTANCE.toUser(request);
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    userRepository.insert(user);
  }
}
