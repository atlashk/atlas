package org.atlas.domain.user.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.domain.user.usecase.admin.model.AdminListUserInput;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.paging.PagingResult;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminListUserUseCaseHandler {

  private final UserRepository userRepository;

  public PagingResult<UserEntity> handle(AdminListUserInput input) throws Exception {
    return userRepository.findAll(input.getPagingRequest());
  }
}
