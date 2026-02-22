package org.atlas.services.identity.port.in.user.service;

import org.atlas.services.identity.port.in.user.model.ProfileOutput;
import org.atlas.services.identity.port.in.user.model.RegisterInput;

public interface UserService {

  void register(RegisterInput input);

  ProfileOutput retrieveProfile();
}
