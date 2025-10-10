package org.atlas.domain.product.infrastructure.file.csv;

import java.util.List;
import org.atlas.domain.product.infrastructure.file.model.read.ProductRow;

public interface ProductCsvReader {

  List<ProductRow> read(byte[] fileContent) throws Exception;
}
