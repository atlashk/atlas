package org.atlas.services.product.port.in.admin.service;

import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.port.in.admin.model.AdminCreateProductInput;
import org.atlas.services.product.port.in.admin.model.AdminExportProductInput;
import org.atlas.services.product.port.in.admin.model.AdminImportProductInput;
import org.atlas.services.product.port.in.admin.model.AdminRetrieveProductListInput;
import org.atlas.services.product.port.in.admin.model.AdminUpdateProductInput;

public interface AdminProductService {

  PagingResult<ProductEntity> retrieveProductList(AdminRetrieveProductListInput input);

  ProductEntity retrieveProduct(String id);

  Long retrieveProductCount();

  String createProduct(AdminCreateProductInput input) throws Exception;

  void updateProduct(AdminUpdateProductInput input) throws Exception;

  void deleteProduct(String id);

  void importProduct(AdminImportProductInput input) throws Exception;

  byte[] exportProduct(AdminExportProductInput input) throws Exception;
}
