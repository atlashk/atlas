package org.atlas.services.user.application.admin.service;

import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.user.application.admin.mapper.AdminUserMapper;
import org.atlas.services.user.application.admin.model.AdminRetrieveUserListInput;
import org.atlas.services.user.application.port.repository.UserRepository;
import org.atlas.services.user.application.port.repository.criteria.FindUserCriteria;
import org.atlas.services.user.domain.entity.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

  private final UserRepository userRepository;

  @Override
  public PagingResult<User> retrieveUserList(AdminRetrieveUserListInput input) {
    FindUserCriteria criteria = AdminUserMapper.INSTANCE.toFindUserCriteria(input);
    return userRepository.findByCriteria(criteria, input.getPagingRequest());
  }

  @Override
  public Long retrieveUserCount() {
    return userRepository.countAll();
  }
}
