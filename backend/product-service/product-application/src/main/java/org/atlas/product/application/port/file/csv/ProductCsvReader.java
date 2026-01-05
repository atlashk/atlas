package org.atlas.product.application.port.file.csv;

import java.util.List;
import org.atlas.product.application.port.file.model.ProductReadRow;

public interface ProductCsvReader {

  List<ProductReadRow> read(byte[] fileContent) throws Exception;
}
