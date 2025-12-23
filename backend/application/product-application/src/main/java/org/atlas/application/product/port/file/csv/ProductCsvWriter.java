package org.atlas.application.product.port.file.csv;

import java.util.List;
import org.atlas.application.product.port.file.model.ProductWriteRow;

public interface ProductCsvWriter {

  byte[] write(List<ProductWriteRow> productRows) throws Exception;
}
