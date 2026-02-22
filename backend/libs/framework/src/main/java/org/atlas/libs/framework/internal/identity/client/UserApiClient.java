package org.atlas.libs.framework.internal.identity.client;

import java.util.List;
import org.atlas.libs.framework.internal.identity.model.RetrieveUserListInput;
import org.atlas.libs.framework.internal.identity.model.UserOutput;

public interface UserApiClient {

  List<UserOutput> call(RetrieveUserListInput request);
}
