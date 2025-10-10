package org.atlas.domain.user.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.domain.user.repository.criteria.FindUserCriteria;
import org.atlas.domain.user.usecase.admin.model.AdminListUserInput;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingResult;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class AdminListUserUseCaseHandler {

  private final UserRepository userRepository;

  public PagingResult<UserEntity> handle(AdminListUserInput input) throws Exception {
    FindUserCriteria criteria = ObjectMapperUtil.getInstance()
        .map(input, FindUserCriteria.class);
    return userRepository.findByCriteria(criteria, input.getPagingRequest());
  }
}
