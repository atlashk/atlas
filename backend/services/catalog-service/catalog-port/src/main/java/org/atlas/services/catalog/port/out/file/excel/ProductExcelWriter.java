package org.atlas.services.catalog.port.out.file.excel;

import java.util.List;
import org.atlas.services.catalog.port.out.file.model.ProductWriteRow;

public interface ProductExcelWriter {

  String SHEET_NAME = "Products";

  byte[] write(List<ProductWriteRow> productRows) throws Exception;
}
