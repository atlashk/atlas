package org.atlas.services.iam.port.in.user.service;

import java.util.List;
import org.atlas.libs.framework.internalapi.iam.model.RetrieveUserListInput;
import org.atlas.libs.framework.internalapi.iam.model.UserOutput;

public interface UserInternalService {

  List<UserOutput> retrieveUserList(RetrieveUserListInput input);
}
