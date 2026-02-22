package org.atlas.services.catalog.port.in.product.service;

import java.util.List;
import org.atlas.libs.framework.internal.product.model.ProductOutput;
import org.atlas.libs.framework.internal.product.model.RetrieveProductListInput;

public interface ProductInternalService {

  List<ProductOutput> retrieveProductList(RetrieveProductListInput input);
}
