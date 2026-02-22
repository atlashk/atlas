package org.atlas.services.catalog.port.in.product.service;

import org.atlas.services.catalog.domain.entity.ProductEntity;

public interface ProductService {

  ProductEntity retrieveProduct(String id) throws Exception;
}
