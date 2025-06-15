package org.atlas.domain.product.port.file.csv;

import java.util.List;
import org.atlas.domain.product.port.file.model.write.ProductRow;

public interface ProductCsvWriterPort {

   byte[] write(List<ProductRow> productRows) throws Exception;
}
