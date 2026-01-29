package org.atlas.services.product.application.port.file.excel;

import java.util.List;
import org.atlas.services.product.application.port.file.model.ProductWriteRow;

public interface ProductExcelWriter {

  String SHEET_NAME = "Products";

  byte[] write(List<ProductWriteRow> productRows) throws Exception;
}
