package org.atlas.application.user.admin.service;

import org.atlas.application.user.admin.model.AdminRetrieveUserListInput;
import org.atlas.domain.user.entity.User;
import org.atlas.framework.paging.PagingResult;

public interface AdminUserService {

  PagingResult<User> retrieveUserList(AdminRetrieveUserListInput input);

  Long retrieveUserCount();
}
