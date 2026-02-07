package org.atlas.services.iam.port.in.admin.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.iam.port.in.admin.model.AdminRetrieveUserListInput;
import org.atlas.services.iam.port.in.admin.model.AdminUserOutput;
import org.atlas.services.iam.port.in.admin.model.AdminCreateUserInput;
import org.atlas.services.iam.port.in.admin.model.AdminUpdateUserInput;

public interface AdminUserService {

  PagingResult<AdminUserOutput> retrieveUserList(AdminRetrieveUserListInput input);

  Long retrieveUserCount();

  AdminUserOutput retrieveUser(String id);

  void createUser(AdminCreateUserInput input);

  void updateUser(AdminUpdateUserInput input);

  void deleteUser(String userId);
}
