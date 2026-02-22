package org.atlas.libs.framework.internal.product.client;

import java.util.List;
import org.atlas.libs.framework.internal.product.model.ProductOutput;
import org.atlas.libs.framework.internal.product.model.RetrieveProductListInput;

public interface ProductApiClient {

  List<ProductOutput> call(RetrieveProductListInput request);
}
