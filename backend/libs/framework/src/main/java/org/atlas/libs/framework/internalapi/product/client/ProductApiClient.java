package org.atlas.libs.framework.internalapi.product.client;

import java.util.List;
import org.atlas.libs.framework.internalapi.product.model.ProductOutput;
import org.atlas.libs.framework.internalapi.product.model.RetrieveProductListInput;

public interface ProductApiClient {

  List<ProductOutput> call(RetrieveProductListInput request);
}
