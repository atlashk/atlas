package org.atlas.libs.framework.internalapi.iam.client;

import java.util.List;
import org.atlas.libs.framework.internalapi.iam.model.RetrieveUserListInput;
import org.atlas.libs.framework.internalapi.iam.model.UserOutput;

public interface UserApiClient {

  List<UserOutput> call(RetrieveUserListInput request);
}
