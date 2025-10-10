package org.atlas.domain.product.infrastructure.file.pdf;

import java.util.List;
import org.atlas.domain.product.infrastructure.file.model.write.ProductRow;

public interface ProductPdfWriter {

  byte[] write(List<ProductRow> productRows) throws Exception;
}
