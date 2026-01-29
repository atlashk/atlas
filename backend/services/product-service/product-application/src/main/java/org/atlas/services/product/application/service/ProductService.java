package org.atlas.services.product.application.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.product.application.model.RetrieveProductListInput;
import org.atlas.services.product.domain.entity.Product;

public interface ProductService {

  PagingResult<Product> retrieveProductList(RetrieveProductListInput input);

  Product retrieveProduct(Integer productId) throws Exception;
}
