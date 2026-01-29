package org.atlas.libs.framework.internalapi.product;

import java.util.List;
import org.atlas.libs.framework.internalapi.product.model.ListProductRequest;
import org.atlas.libs.framework.internalapi.product.model.ProductResponse;

public interface ProductApiClient {

  List<ProductResponse> call(ListProductRequest request);
}
