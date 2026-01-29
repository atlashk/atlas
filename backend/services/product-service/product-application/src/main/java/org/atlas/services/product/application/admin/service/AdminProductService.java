package org.atlas.services.product.application.admin.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.product.application.admin.model.AdminCreateProductInput;
import org.atlas.services.product.application.admin.model.AdminExportProductInput;
import org.atlas.services.product.application.admin.model.AdminImportProductInput;
import org.atlas.services.product.application.admin.model.AdminRetrieveProductListInput;
import org.atlas.services.product.application.admin.model.AdminUpdateProductInput;
import org.atlas.services.product.domain.entity.Product;

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
