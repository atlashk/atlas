package org.atlas.services.catalog.port.in.product.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.port.in.product.model.RetrieveProductListInput;

public interface ProductService {

  PagingResult<ProductEntity> retrieveProductList(RetrieveProductListInput input);
  
  ProductEntity retrieveProduct(String id) throws Exception;
}
