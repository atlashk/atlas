package org.atlas.services.iam.port.in.internal.service;

import java.util.List;
import org.atlas.services.iam.port.in.internal.model.InternalRetrieveUserListInput;
import org.atlas.services.iam.port.in.internal.model.InternalUserOutput;

public interface InternalUserService {

  List<InternalUserOutput> retrieveUserList(InternalRetrieveUserListInput input);
}
