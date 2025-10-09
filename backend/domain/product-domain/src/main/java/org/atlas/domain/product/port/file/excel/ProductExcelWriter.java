package org.atlas.domain.product.port.file.excel;

import java.util.List;
import org.atlas.domain.product.port.file.model.write.ProductRow;

public interface ProductExcelWriter {

  String SHEET_NAME = "Products";

  byte[] write(List<ProductRow> productRows) throws Exception;
}
