package org.atlas.product.application.admin.service;

import org.atlas.product.application.admin.model.AdminCreateProductInput;
import org.atlas.product.application.admin.model.AdminExportProductInput;
import org.atlas.product.application.admin.model.AdminImportProductInput;
import org.atlas.product.application.admin.model.AdminRetrieveProductListInput;
import org.atlas.product.application.admin.model.AdminUpdateProductInput;
import org.atlas.product.domain.entity.Product;
import org.atlas.common.framework.paging.PagingResult;

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
