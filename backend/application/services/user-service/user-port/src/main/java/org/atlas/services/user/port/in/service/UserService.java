package org.atlas.services.user.port.in.service;

import org.atlas.services.user.port.in.model.ProfileOutput;
import org.atlas.services.user.port.in.model.RegisterInput;

public interface UserService {

  void register(RegisterInput input);

  ProfileOutput retrieveProfile();
}
