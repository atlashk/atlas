package org.atlas.product.application.internal.service;

import java.util.List;
import org.atlas.product.application.internal.model.InternalRetrieveProductListInput;
import org.atlas.product.domain.entity.Product;

public interface InternalProductService {

  List<Product> retrieveProductList(InternalRetrieveProductListInput input);
}
