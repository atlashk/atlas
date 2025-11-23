package org.atlas.domain.product.infrastructure.file.csv;

import java.util.List;
import org.atlas.domain.product.infrastructure.file.model.ProductWriteRow;

public interface ProductCsvWriter {

  byte[] write(List<ProductWriteRow> productRows) throws Exception;
}
