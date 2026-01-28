package org.atlas.common.framework.internalapi.user;

import org.atlas.common.framework.internalapi.user.model.CartResponse;
import org.atlas.common.framework.internalapi.user.model.GetCartRequest;

public interface CartApiClient {

  CartResponse call(GetCartRequest request);
}
