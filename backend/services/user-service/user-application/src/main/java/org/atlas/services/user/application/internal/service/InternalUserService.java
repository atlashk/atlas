package org.atlas.services.user.application.internal.service;

import java.util.List;
import org.atlas.services.user.application.internal.model.InternalRetrieveUserListInput;
import org.atlas.services.user.domain.entity.Cart;
import org.atlas.services.user.domain.entity.User;

public interface InternalUserService {

  Cart retrieveCart(Integer userId);

  List<User> retrieveUserList(InternalRetrieveUserListInput input);
}
