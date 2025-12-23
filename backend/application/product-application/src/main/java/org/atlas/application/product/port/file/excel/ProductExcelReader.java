package org.atlas.application.product.port.file.excel;

import java.util.List;
import org.atlas.application.product.port.file.model.ProductReadRow;

public interface ProductExcelReader {

  String SHEET_NAME = "Products";

  List<ProductReadRow> read(byte[] fileContent) throws Exception;
}
