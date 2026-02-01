package org.atlas.libs.framework.internalapi.iam.client;

import java.util.List;
import org.atlas.libs.framework.internalapi.iam.model.ListUserRequest;
import org.atlas.libs.framework.internalapi.iam.model.UserResponse;

public interface UserApiClient {

  List<UserResponse> call(ListUserRequest request);
}
