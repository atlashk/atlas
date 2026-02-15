package org.atlas.services.iam.port.in.user.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.iam.port.in.user.model.admin.RetrieveUserListInput;
import org.atlas.services.iam.port.in.user.model.admin.UpdateUserInput;
import org.atlas.services.iam.port.in.user.model.admin.UserOutput;
import org.atlas.services.iam.port.in.user.model.admin.CreateUserInput;

public interface UserAdminService {

  PagingResult<UserOutput> retrieveUserList(RetrieveUserListInput input);

  Long retrieveUserCount();

  UserOutput retrieveUser(String id);

  void createUser(CreateUserInput input);

  void updateUser(UpdateUserInput input);

  void deleteUser(String id);

  boolean existsUser(String username);
}
