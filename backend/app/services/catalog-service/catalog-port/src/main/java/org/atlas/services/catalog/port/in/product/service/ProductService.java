package org.atlas.services.catalog.port.in.product.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.catalog.domain.entity.Product;
import org.atlas.services.catalog.port.in.product.model.RetrieveProductListInput;

public interface ProductService {

  PagingResult<Product> retrieveProductList(RetrieveProductListInput input);

  Product retrieveProduct(String id) throws Exception;
}
