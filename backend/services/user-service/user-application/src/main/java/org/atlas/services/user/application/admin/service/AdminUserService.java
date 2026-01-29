package org.atlas.services.user.application.admin.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.user.application.admin.model.AdminRetrieveUserListInput;
import org.atlas.services.user.domain.entity.User;

public interface AdminUserService {

  PagingResult<User> retrieveUserList(AdminRetrieveUserListInput input);

  Long retrieveUserCount();
}
