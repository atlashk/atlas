package org.atlas.libs.framework.internalapi.user;

import java.util.List;
import org.atlas.libs.framework.internalapi.user.model.ListUserRequest;
import org.atlas.libs.framework.internalapi.user.model.UserResponse;

public interface UserApiClient {

  List<UserResponse> call(ListUserRequest request);
}
