package org.atlas.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.entity.User;
import org.atlas.domain.auth.mapper.UserMapper;
import org.atlas.domain.auth.model.CreateUserRequest;
import org.atlas.domain.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public void createUser(CreateUserRequest request) {
    User user = UserMapper.INSTANCE.toUser(request);
    userRepository.insert(user);
  }
}
