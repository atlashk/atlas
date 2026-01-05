package org.atlas.user.application.admin.service;

import org.atlas.user.application.admin.model.AdminRetrieveUserListInput;
import org.atlas.user.domain.entity.User;
import org.atlas.common.framework.paging.PagingResult;

public interface AdminUserService {

  PagingResult<User> retrieveUserList(AdminRetrieveUserListInput input);

  Long retrieveUserCount();
}
