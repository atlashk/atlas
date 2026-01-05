package org.atlas.product.application.service;

import org.atlas.product.application.model.RetrieveProductListInput;
import org.atlas.product.domain.entity.Product;
import org.atlas.common.framework.paging.PagingResult;

public interface ProductService {

  PagingResult<Product> retrieveProductList(RetrieveProductListInput input);

  Product retrieveProduct(Integer productId) throws Exception;
}
