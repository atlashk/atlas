package org.atlas.framework.internalapi.user;

import org.atlas.framework.internalapi.user.model.CartResponse;
import org.atlas.framework.internalapi.user.model.GetCartRequest;

public interface CartApiClient {

  CartResponse call(GetCartRequest request);
}
