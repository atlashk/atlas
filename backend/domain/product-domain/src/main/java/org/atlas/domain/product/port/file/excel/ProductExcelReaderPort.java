package org.atlas.domain.product.port.file.excel;

import java.util.List;
import org.atlas.domain.product.port.file.model.read.ProductRow;

public interface ProductExcelReaderPort {

  String SHEET_NAME = "Products";

  List<ProductRow> read(byte[] fileContent) throws Exception;
}
