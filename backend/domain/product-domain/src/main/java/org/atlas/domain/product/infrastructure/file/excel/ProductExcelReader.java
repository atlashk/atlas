package org.atlas.domain.product.infrastructure.file.excel;

import java.util.List;
import org.atlas.domain.product.infrastructure.file.model.read.ProductRow;

public interface ProductExcelReader {

  String SHEET_NAME = "Products";

  List<ProductRow> read(byte[] fileContent) throws Exception;
}
