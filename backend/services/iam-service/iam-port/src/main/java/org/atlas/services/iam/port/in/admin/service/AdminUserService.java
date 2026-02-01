package org.atlas.services.iam.port.in.admin.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.iam.domain.entity.User;
import org.atlas.services.iam.port.in.admin.model.AdminRetrieveUserListInput;
import org.atlas.services.iam.port.in.admin.model.AdminUserOutput;
import org.atlas.services.iam.port.in.front.model.ChangePasswordInput;
import org.atlas.services.iam.port.in.admin.model.AdminCreateUserInput;
import org.atlas.services.iam.port.in.admin.model.AdminUpdateUserInput;

public interface AdminUserService {

  PagingResult<AdminUserOutput> retrieveUserList(AdminRetrieveUserListInput input);

  Long retrieveUserCount();

  AdminUserOutput retrieveUser(Integer userId);

  void createUser(AdminCreateUserInput input);

  void updateUser(AdminUpdateUserInput input);

  void deleteUser(Integer userId);
}
