package org.atlas.application.product.admin.service;

import org.atlas.application.product.admin.model.AdminCreateProductInput;
import org.atlas.application.product.admin.model.AdminExportProductInput;
import org.atlas.application.product.admin.model.AdminImportProductInput;
import org.atlas.application.product.admin.model.AdminRetrieveProductListInput;
import org.atlas.application.product.admin.model.AdminUpdateProductInput;
import org.atlas.domain.product.entity.Product;
import org.atlas.framework.paging.PagingResult;

public interface AdminProductService {

  PagingResult<Product> retrieveProductList(AdminRetrieveProductListInput input);

  Product retrieveProduct(Integer productId);

  Long retrieveProductCount();

  Integer createProduct(AdminCreateProductInput input) throws Exception;

  void updateProduct(AdminUpdateProductInput input) throws Exception;

  void deleteProduct(Integer productId);

  void importProduct(AdminImportProductInput input) throws Exception;

  byte[] exportProduct(AdminExportProductInput input) throws Exception;
}
