package org.atlas.services.iam.application.jwt.internal.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.iam.application.jwt.internal.mapper.InternalUserMapper;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.in.internal.model.InternalRetrieveUserListInput;
import org.atlas.services.iam.port.in.internal.model.InternalUserOutput;
import org.atlas.services.iam.port.in.internal.service.InternalUserService;
import org.atlas.services.iam.port.out.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalUserServiceImpl implements InternalUserService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public List<InternalUserOutput> retrieveUserList(InternalRetrieveUserListInput input) {
    List<UserEntity> userList = userRepository.findByIdIn(input.getIds());
    return MapperUtil.mapList(userList, InternalUserMapper.INSTANCE::toInternalUserOutput);
  }
}
