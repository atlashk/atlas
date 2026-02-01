package org.atlas.libs.framework.internalapi.catalog.client;

import java.util.List;
import org.atlas.libs.framework.internalapi.catalog.model.ListProductRequest;
import org.atlas.libs.framework.internalapi.catalog.model.ProductResponse;

public interface ProductApiClient {

  List<ProductResponse> call(ListProductRequest request);
}
