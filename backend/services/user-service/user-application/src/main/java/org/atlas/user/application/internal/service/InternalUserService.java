package org.atlas.user.application.internal.service;

import java.util.List;
import org.atlas.user.application.internal.model.InternalRetrieveUserListInput;
import org.atlas.user.domain.entity.Cart;
import org.atlas.user.domain.entity.User;

public interface InternalUserService {

  Cart retrieveCart(Integer userId);

  List<User> retrieveUserList(InternalRetrieveUserListInput input);
}
