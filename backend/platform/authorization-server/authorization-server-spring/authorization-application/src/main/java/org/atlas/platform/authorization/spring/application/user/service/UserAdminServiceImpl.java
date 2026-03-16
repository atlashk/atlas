package org.atlas.platform.authorization.spring.application.user.service;

import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.platform.authorization.domain.entity.UserEntity;
import org.atlas.platform.authorization.domain.error.DomainError;
import org.atlas.platform.authorization.domain.exception.DomainException;
import org.atlas.platform.authorization.port.in.user.model.admin.CreateUserInput;
import org.atlas.platform.authorization.port.in.user.model.admin.RetrieveUserListInput;
import org.atlas.platform.authorization.port.in.user.model.admin.UpdateUserInput;
import org.atlas.platform.authorization.port.in.user.model.admin.UserOutput;
import org.atlas.platform.authorization.port.in.user.service.UserAdminService;
import org.atlas.platform.authorization.port.out.repository.UserRepository;
import org.atlas.platform.authorization.port.out.repository.UserRepository.FindUserCriteria;
import org.atlas.platform.authorization.spring.application.user.mapper.UserAdminMapper;
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
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
  }

  @Override
  @Transactional
  public String createUser(CreateUserInput input) {
    checkValidity(input);

    UserEntity user = UserAdminMapper.INSTANCE.toUser(input);
    user.setId(sequenceGenerator.generate(SequenceType.USER));
    user.setPassword(passwordEncoder.encode(input.getPassword()));
    userRepository.insert(user);

    return user.getId();
  }

  @Override
  @Transactional
  public void updateUser(UpdateUserInput input) {
    UserEntity user = userRepository.findById(input.getId())
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));

    UserAdminMapper.INSTANCE.merge(input, user);
    userRepository.update(user);
  }

  @Override
  @Transactional
  public void deleteUser(String id) {
    userRepository.deleteById(id);
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
      throw new DomainException(DomainError.EMAIL_ALREADY_EXISTS);
    }

    if (StringUtil.isNotBlank(input.getPhone()) &&
        userRepository.existsByPhone(input.getPhone())) {
      throw new DomainException(DomainError.PHONE_NUMBER_ALREADY_EXISTS);
    }
  }
}
