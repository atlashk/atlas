package org.atlas.product.application.port.file.csv;

import java.util.List;
import org.atlas.product.application.port.file.model.ProductWriteRow;

public interface ProductCsvWriter {

  byte[] write(List<ProductWriteRow> productRows) throws Exception;
}
