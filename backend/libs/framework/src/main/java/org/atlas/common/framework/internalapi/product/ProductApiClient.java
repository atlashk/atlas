package org.atlas.common.framework.internalapi.product;

import java.util.List;
import org.atlas.common.framework.internalapi.product.model.ListProductRequest;
import org.atlas.common.framework.internalapi.product.model.ProductResponse;

public interface ProductApiClient {

  List<ProductResponse> call(ListProductRequest request);
}
