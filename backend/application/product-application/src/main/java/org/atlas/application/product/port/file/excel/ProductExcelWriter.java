package org.atlas.application.product.port.file.excel;

import java.util.List;
import org.atlas.application.product.port.file.model.ProductWriteRow;

public interface ProductExcelWriter {

  String SHEET_NAME = "Products";

  byte[] write(List<ProductWriteRow> productRows) throws Exception;
}
