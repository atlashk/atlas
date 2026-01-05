package org.atlas.product.application.port.file.excel;

import java.util.List;
import org.atlas.product.application.port.file.model.ProductReadRow;

public interface ProductExcelReader {

  String SHEET_NAME = "Products";

  List<ProductReadRow> read(byte[] fileContent) throws Exception;
}
