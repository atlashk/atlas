package org.atlas.services.identity.application.keycloak.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.security.authorization.RequiredAdmin;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.identity.application.keycloak.core.client.KeycloakUserClient;
import org.atlas.services.identity.application.keycloak.user.mapper.UserAdminMapper;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.atlas.services.identity.domain.error.DomainError;
import org.atlas.services.identity.domain.exception.DomainException;
import org.atlas.services.identity.port.in.user.model.admin.CreateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.RetrieveUserListInput;
import org.atlas.services.identity.port.in.user.model.admin.UpdateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.UserOutput;
import org.atlas.services.identity.port.in.user.service.UserAdminService;
import org.atlas.services.identity.port.out.repository.UserRepository;
import org.atlas.services.identity.port.out.repository.UserRepository.FindUserCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredAdmin
@RequiredArgsConstructor
@Slf4j
public class UserAdminServiceImpl implements UserAdminService {

  private final UserRepository userRepository;
  private final KeycloakUserClient keycloakUserClient;

  @Override
  @Transactional(readOnly = true)
  public PagingResult<UserOutput> retrieveUserList(RetrieveUserListInput input) {
    FindUserCriteria criteria = UserAdminMapper.INSTANCE.toFindUserCriteria(input);
    PagingResult<UserEntity> userPage = userRepository.findByCriteria(criteria,
        input.getPagingRequest());
    return MapperUtil.mapPage(userPage, UserAdminMapper.INSTANCE::toUserOutput);
  }

  @Override
  @Transactional(readOnly = true)
  public UserOutput retrieveUser(String id) {
    return userRepository.findById(id)
        .map(UserAdminMapper.INSTANCE::toUserOutput)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
  }

  @Override
  @Transactional
  public String createUser(CreateUserInput input) {
    checkValidity(input);

    UserEntity user = UserAdminMapper.INSTANCE.toUser(input);

    // 1. Create Keycloak user
    String kcUserId;
    try {
      kcUserId = keycloakUserClient.createUser(user, input.getPassword());
    } catch (Exception e) {
      log.error("Failed to sync user to Keycloak: username={}, error={}", 
          user.getUsername(), e.getMessage(), e);
      throw new DomainException(DomainError.USER_REGISTRATION_FAILED, e);
    }

    // 2. Save to DB (without password - Keycloak handles password)
    user.setId(kcUserId);
    userRepository.insert(user);

    return kcUserId;
  }

  @Override
  @Transactional
  public void updateUser(UpdateUserInput input) {
    UserEntity user = userRepository.findById(input.getId())
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
    UserAdminMapper.INSTANCE.merge(input, user);

    // 1. Sync to Keycloak
    try {
      keycloakUserClient.updateUser(user);
    } catch (Exception e) {
      log.error("Failed to sync user update to Keycloak: userId={}, error={}", 
          user.getId(), e.getMessage(), e);
      throw new DomainException(DomainError.USER_UPDATE_FAILED, e);
    }

    // Save to DB
    userRepository.update(user);
  }

  @Override
  @Transactional
  public void deleteUser(String id) {
    UserEntity user = userRepository.findById(id)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
    
    // 1. Delete from Keycloak
    try {
      keycloakUserClient.deleteUser(user.getId());
    } catch (Exception e) {
      log.error("Failed to delete user from Keycloak: userId={}, error={}", 
          user.getId(), e.getMessage(), e);
      throw new DomainException(DomainError.USER_DELETE_FAILED, e);
    }
    
    // 2. Delete from DB
    userRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsUser(String username) {
    return userRepository.existsByUsername(username);
  }

  @Override
  @Transactional(readOnly = true)
  public Long retrieveTotalUserCount() {
    return userRepository.countAll();
  }

  private void checkValidity(CreateUserInput input) {
    if (userRepository.existsByUsername(input.getUsername())) {
      throw new DomainException(DomainError.USERNAME_ALREADY_EXISTS);
    }
    if (userRepository.existsByEmail(input.getEmail())) {
      throw new DomainException(DomainError.EMAIL_ALREADY_EXISTS);
    }
    if (userRepository.existsByPhoneNumber(input.getPhoneNumber())) {
      throw new DomainException(DomainError.PHONE_NUMBER_ALREADY_EXISTS);
    }
  }
}
