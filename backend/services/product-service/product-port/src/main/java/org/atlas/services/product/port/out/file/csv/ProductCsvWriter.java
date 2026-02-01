package org.atlas.services.product.port.out.file.csv;

import java.util.List;
import org.atlas.services.product.port.out.file.model.ProductWriteRow;

public interface ProductCsvWriter {

  byte[] write(List<ProductWriteRow> productRows) throws Exception;
}
