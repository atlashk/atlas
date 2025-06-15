package org.atlas.framework.internalapi.product;

import java.util.List;
import org.atlas.framework.internalapi.product.model.ListProductRequest;
import org.atlas.framework.internalapi.product.model.ProductResponse;

public interface ProductApiPort {

  List<ProductResponse> call(ListProductRequest request);
}
