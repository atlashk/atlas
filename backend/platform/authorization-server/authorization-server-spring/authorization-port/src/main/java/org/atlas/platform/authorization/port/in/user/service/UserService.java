package org.atlas.platform.authorization.port.in.user.service;

import org.atlas.platform.authorization.port.in.user.model.ProfileOutput;
import org.atlas.platform.authorization.port.in.user.model.RegisterInput;

public interface UserService {

  void register(RegisterInput input);

  ProfileOutput retrieveProfile();
}
