package org.atlas.domain.product.infrastructure.file.excel;

import java.util.List;
import org.atlas.domain.product.infrastructure.file.model.ProductWriteRow;

public interface ProductExcelWriter {

  String SHEET_NAME = "Products";

  byte[] write(List<ProductWriteRow> productRows) throws Exception;
}
