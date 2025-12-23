package org.atlas.application.user.admin.service;

import lombok.RequiredArgsConstructor;
import org.atlas.application.user.admin.mapper.AdminUserMapper;
import org.atlas.application.user.admin.model.AdminRetrieveUserListInput;
import org.atlas.application.user.port.repository.UserRepository;
import org.atlas.application.user.port.repository.criteria.FindUserCriteria;
import org.atlas.domain.user.entity.User;
import org.atlas.framework.paging.PagingResult;
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
