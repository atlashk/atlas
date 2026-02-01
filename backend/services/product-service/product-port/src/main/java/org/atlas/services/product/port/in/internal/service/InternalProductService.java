package org.atlas.services.product.port.in.internal.service;

import java.util.List;
import org.atlas.services.product.port.in.internal.model.InternalRetrieveProductListInput;
import org.atlas.services.product.domain.entity.Product;

public interface InternalProductService {

  List<Product> retrieveProductList(InternalRetrieveProductListInput input);
}
