package org.atlas.libs.framework.internal.catalog.client;

import java.util.List;
import org.atlas.libs.framework.internal.catalog.model.ProductOutput;
import org.atlas.libs.framework.internal.catalog.model.RetrieveProductListInput;

public interface ProductApiClient {

  List<ProductOutput> call(RetrieveProductListInput request);
}
