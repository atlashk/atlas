package org.atlas.services.iam.port.in.front.service;

import org.atlas.services.iam.port.in.front.model.ChangePasswordInput;
import org.atlas.services.iam.port.in.front.model.ProfileOutput;
import org.atlas.services.iam.port.in.front.model.RegisterInput;

public interface UserService {

  void register(RegisterInput input);

  ProfileOutput retrieveProfile();

  void changePassword(ChangePasswordInput input);
}
