package org.atlas.libs.framework.internalapi.user;

import org.atlas.libs.framework.internalapi.user.model.CartResponse;
import org.atlas.libs.framework.internalapi.user.model.GetCartRequest;

public interface CartApiClient {

  CartResponse call(GetCartRequest request);
}
