package org.atlas.services.catalog.port.in.product.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.catalog.domain.entity.Product;
import org.atlas.services.catalog.port.in.product.model.admin.CreateProductInput;
import org.atlas.services.catalog.port.in.product.model.admin.ExportProductInput;
import org.atlas.services.catalog.port.in.product.model.admin.ImportProductInput;
import org.atlas.services.catalog.port.in.product.model.admin.RetrieveProductListInput;
import org.atlas.services.catalog.port.in.product.model.admin.UpdateProductInput;

public interface ProductAdminService {

  PagingResult<Product> retrieveProductList(RetrieveProductListInput input);

  Product retrieveProduct(String id);

  String createProduct(CreateProductInput input) throws Exception;

  void updateProduct(UpdateProductInput input) throws Exception;

  void deleteProduct(String id);

  void importProduct(ImportProductInput input) throws Exception;

  byte[] exportProduct(ExportProductInput input) throws Exception;

  Long retrieveTotalProductCount();
}
