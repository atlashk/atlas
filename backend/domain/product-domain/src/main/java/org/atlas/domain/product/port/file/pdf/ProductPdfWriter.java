package org.atlas.domain.product.port.file.pdf;

import java.util.List;
import org.atlas.domain.product.port.file.model.write.ProductRow;

public interface ProductPdfWriter {

  byte[] write(List<ProductRow> productRows) throws Exception;
}
