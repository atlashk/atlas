package org.atlas.services.user.port.in.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.user.port.in.model.admin.CreateUserInput;
import org.atlas.services.user.port.in.model.admin.RetrieveUserListInput;
import org.atlas.services.user.port.in.model.admin.UpdateUserInput;
import org.atlas.services.user.port.in.model.admin.UserOutput;

public interface UserAdminService {

  PagingResult<UserOutput> retrieveUserList(RetrieveUserListInput input);

  UserOutput retrieveUser(String id);

  String createUser(CreateUserInput input);

  void updateUser(UpdateUserInput input);

  void deleteUser(String id);

  boolean existsUser(String email);

  Long retrieveTotalUserCount();
}
