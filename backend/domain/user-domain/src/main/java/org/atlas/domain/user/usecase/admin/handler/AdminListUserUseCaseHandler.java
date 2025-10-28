package org.atlas.domain.user.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.User;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.domain.user.repository.criteria.FindUserCriteria;
import org.atlas.domain.user.usecase.admin.mapper.AdminUserMapper;
import org.atlas.domain.user.usecase.admin.model.AdminListUserInput;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;
import org.atlas.framework.paging.PagingResult;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class AdminListUserUseCaseHandler {

  private final UserRepository userRepository;

  public PagingResult<User> handle(AdminListUserInput input) throws Exception {
    FindUserCriteria criteria = AdminUserMapper.INSTANCE.toFindUserCriteria(input);
    return userRepository.findByCriteria(criteria, input.getPagingRequest());
  }
}
