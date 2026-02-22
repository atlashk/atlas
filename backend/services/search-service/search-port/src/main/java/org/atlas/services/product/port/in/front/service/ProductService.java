package org.atlas.services.product.port.in.front.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.product.port.in.front.model.RetrieveProductListInput;
import org.atlas.services.product.domain.entity.ProductEntity;

public interface ProductService {

  PagingResult<ProductEntity> retrieveProductList(RetrieveProductListInput input);

  ProductEntity retrieveProduct(String id) throws Exception;
}
