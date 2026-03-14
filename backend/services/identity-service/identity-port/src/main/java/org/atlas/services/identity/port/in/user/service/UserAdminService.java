package org.atlas.services.identity.port.in.user.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.identity.port.in.user.model.admin.CreateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.RetrieveUserListInput;
import org.atlas.services.identity.port.in.user.model.admin.UpdateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.UserOutput;

public interface UserAdminService {

  PagingResult<UserOutput> retrieveUserList(RetrieveUserListInput input);

  UserOutput retrieveUser(String id);

  String createUser(CreateUserInput input);

  void updateUser(UpdateUserInput input);

  void deleteUser(String id);

  boolean existsUser(String email);

  Long retrieveTotalUserCount();
}
