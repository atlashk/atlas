package org.atlas.application.product.internal.service;

import java.util.List;
import org.atlas.application.product.internal.model.InternalRetrieveProductListInput;
import org.atlas.domain.product.entity.Product;

public interface InternalProductService {

  List<Product> retrieveProductList(InternalRetrieveProductListInput input);
}
