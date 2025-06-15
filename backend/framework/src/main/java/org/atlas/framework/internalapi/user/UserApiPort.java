package org.atlas.framework.internalapi.user;

import java.util.List;
import org.atlas.framework.internalapi.user.model.ListUserRequest;
import org.atlas.framework.internalapi.user.model.UserResponse;

public interface UserApiPort {

  List<UserResponse> call(ListUserRequest request);
}
