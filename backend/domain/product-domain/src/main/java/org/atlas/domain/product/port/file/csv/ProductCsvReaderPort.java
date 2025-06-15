package org.atlas.domain.product.port.file.csv;

import java.util.List;
import org.atlas.domain.product.port.file.model.read.ProductRow;

public interface ProductCsvReaderPort {

  List<ProductRow> read(byte[] fileContent) throws Exception;
}
