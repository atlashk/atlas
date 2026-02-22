package org.atlas.services.product.port.in.internal.service;

import java.util.List;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.port.in.internal.model.InternalRetrieveProductListInput;

public interface InternalProductService {

  List<ProductEntity> retrieveProductList(InternalRetrieveProductListInput input);
}
