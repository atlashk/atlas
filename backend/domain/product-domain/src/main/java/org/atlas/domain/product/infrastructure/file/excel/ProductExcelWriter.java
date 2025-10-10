package org.atlas.domain.product.infrastructure.file.excel;

import java.util.List;
import org.atlas.domain.product.infrastructure.file.model.write.ProductRow;

public interface ProductExcelWriter {

  String SHEET_NAME = "Products";

  byte[] write(List<ProductRow> productRows) throws Exception;
}
