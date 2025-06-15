package org.atlas.edge.auth.springsecurityjwt.service;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.entity.UserEntity;
import org.atlas.domain.auth.repository.UserRepository;
import org.atlas.edge.auth.springsecurityjwt.model.CreateUserRequest;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.cryptography.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public void createUser(CreateUserRequest request) {
    UserEntity userEntity = ObjectMapperUtil.getInstance()
        .map(request, UserEntity.class);
    userEntity.setPassword(PasswordUtil.hashPassword(request.getPassword()));
    userRepository.insert(userEntity);
  }
}
