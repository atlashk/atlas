package org.atlas.domain.product.infrastructure.file.excel;

import java.util.List;
import org.atlas.domain.product.infrastructure.file.model.ProductReadRow;

public interface ProductExcelReader {

  String SHEET_NAME = "Products";

  List<ProductReadRow> read(byte[] fileContent) throws Exception;
}
