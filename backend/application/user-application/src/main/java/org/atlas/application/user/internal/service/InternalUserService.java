package org.atlas.application.user.internal.service;

import java.util.List;
import org.atlas.application.user.internal.model.InternalRetrieveUserListInput;
import org.atlas.domain.user.entity.Cart;
import org.atlas.domain.user.entity.User;

public interface InternalUserService {

  Cart retrieveCart(Integer userId);

  List<User> retrieveUserList(InternalRetrieveUserListInput input);
}
