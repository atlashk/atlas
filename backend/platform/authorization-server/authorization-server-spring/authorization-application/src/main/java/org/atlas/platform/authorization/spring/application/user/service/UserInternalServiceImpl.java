package org.atlas.platform.authorization.spring.application.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.internal.identity.model.RetrieveUserListInput;
import org.atlas.libs.framework.internal.identity.model.UserOutput;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.platform.authorization.spring.application.user.mapper.UserInternalMapper;
import org.atlas.platform.authorization.domain.entity.UserEntity;
import org.atlas.platform.authorization.port.in.user.service.UserInternalService;
import org.atlas.platform.authorization.port.out.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInternalServiceImpl implements UserInternalService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public List<UserOutput> retrieveUserList(RetrieveUserListInput input) {
    List<UserEntity> userList = userRepository.findByIdIn(input.getIds());
    return MapperUtil.mapList(userList, UserInternalMapper.INSTANCE::toUserOutput);
  }
}
