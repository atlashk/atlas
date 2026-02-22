package org.atlas.services.product.port.out.file.csv;

import java.util.List;
import org.atlas.services.product.port.out.file.model.ProductReadRow;

public interface ProductCsvReader {

  List<ProductReadRow> read(byte[] fileContent) throws Exception;
}
