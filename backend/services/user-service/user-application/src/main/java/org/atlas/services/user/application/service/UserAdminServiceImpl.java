package org.atlas.services.user.application.service;

import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.user.application.mapper.UserAdminMapper;
import org.atlas.services.user.domain.entity.UserEntity;
import org.atlas.services.user.domain.error.UserDomainError;
import org.atlas.services.user.port.in.model.admin.CreateUserInput;
import org.atlas.services.user.port.in.model.admin.RetrieveUserListInput;
import org.atlas.services.user.port.in.model.admin.UpdateUserInput;
import org.atlas.services.user.port.in.model.admin.UserOutput;
import org.atlas.services.user.port.in.service.UserAdminService;
import org.atlas.services.user.port.out.idp.IdpService;
import org.atlas.services.user.port.out.repository.UserRepository;
import org.atlas.services.user.port.out.repository.UserRepository.FindUserCriteria;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

  private final UserRepository userRepository;
  private final SequenceGenerator sequenceGenerator;
  private final PasswordEncoder passwordEncoder;
  private final ObjectProvider<IdpService> idpServiceProvider;

  @Override
  @Transactional(readOnly = true)
  public PagingResult<UserOutput> retrieveUserList(RetrieveUserListInput input) {
    FindUserCriteria criteria = UserAdminMapper.INSTANCE.toFindUserCriteria(input);
    PagingResult<UserEntity> userPage = userRepository.findByCriteria(criteria,
        input.getPagingRequest());
    return MapperUtil.mapPage(userPage, UserAdminMapper.INSTANCE::toUser);
  }

  @Override
  @Transactional(readOnly = true)
  public UserOutput retrieveUser(String id) {
    return userRepository.findById(id)
        .map(UserAdminMapper.INSTANCE::toUser)
        .orElseThrow(() -> new DomainException(UserDomainError.USER_NOT_FOUND));
  }

  @Override
  @Transactional
  public String createUser(CreateUserInput input) {
    checkValidity(input);

    UserEntity user = UserAdminMapper.INSTANCE.toUser(input);
    user.setId(sequenceGenerator.generate(SequenceType.USER));
    user.setPassword(passwordEncoder.encode(input.getPassword()));
    userRepository.insert(user);

    // Synchronize user to IdP if available
    IdpService idpService = idpServiceProvider.getIfAvailable();
    if (idpService != null) {
      // Create new user if not exist
      if (idpService.existsByEmail(input.getEmail())) {
        idpService.updateUser(user);
      } else {
        idpService.createUser(user, input.getPassword());
      }
    }

    return user.getId();
  }

  @Override
  @Transactional
  public void updateUser(UpdateUserInput input) {
    UserEntity user = userRepository.findById(input.getId())
        .orElseThrow(() -> new DomainException(UserDomainError.USER_NOT_FOUND));

    UserAdminMapper.INSTANCE.merge(input, user);
    userRepository.update(user);

    // Synchronize user to IdP if available
    IdpService idpService = idpServiceProvider.getIfAvailable();
    if (idpService != null) {
      idpService.updateUser(user);
    }
  }

  @Override
  @Transactional
  public void deleteUser(String id) {
    UserEntity user = userRepository.findById(id)
        .orElseThrow(() -> new DomainException(UserDomainError.USER_NOT_FOUND));

    userRepository.deleteById(user.getId());

    // Synchronize user to IdP if available
    IdpService idpService = idpServiceProvider.getIfAvailable();
    if (idpService != null) {
      idpService.deleteUser(user.getIdpUserId());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsUser(String email) {
    return userRepository.existsByEmail(email);
  }

  @Override
  @Transactional(readOnly = true)
  public Long retrieveTotalUserCount() {
    return userRepository.countAll();
  }

  private void checkValidity(CreateUserInput input) {
    if (userRepository.existsByEmail(input.getEmail())) {
      throw new DomainException(UserDomainError.EMAIL_ALREADY_EXISTS);
    }

    if (StringUtil.isNotBlank(input.getPhone()) &&
        userRepository.existsByPhone(input.getPhone())) {
      throw new DomainException(UserDomainError.PHONE_NUMBER_ALREADY_EXISTS);
    }
  }
}
