package org.atlas.services.identity.port.in.user.service;

import java.util.List;
import org.atlas.libs.framework.internal.identity.model.RetrieveUserListInput;
import org.atlas.libs.framework.internal.identity.model.UserOutput;

public interface UserInternalService {

  List<UserOutput> retrieveUserList(RetrieveUserListInput input);
}
