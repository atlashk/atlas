package org.atlas.common.framework.internalapi.user;

import java.util.List;
import org.atlas.common.framework.internalapi.user.model.ListUserRequest;
import org.atlas.common.framework.internalapi.user.model.UserResponse;

public interface UserApiClient {

  List<UserResponse> call(ListUserRequest request);
}
