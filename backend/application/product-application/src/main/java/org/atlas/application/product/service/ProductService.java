package org.atlas.application.product.service;

import org.atlas.application.product.model.RetrieveProductListInput;
import org.atlas.domain.product.entity.Product;
import org.atlas.framework.paging.PagingResult;

public interface ProductService {

  PagingResult<Product> retrieveProductList(RetrieveProductListInput input);

  Product retrieveProduct(Integer productId) throws Exception;
}
