package org.atlas.domain.product.infrastructure.file.csv;

import java.util.List;
import org.atlas.domain.product.infrastructure.file.model.write.ProductRow;

public interface ProductCsvWriter {

  byte[] write(List<ProductRow> productRows) throws Exception;
}
