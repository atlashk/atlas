package org.atlas.domain.product.infrastructure.file.pdf;

import java.util.List;
import org.atlas.domain.product.infrastructure.file.model.ProductWriteRow;

public interface ProductPdfWriter {

  byte[] write(List<ProductWriteRow> productRows) throws Exception;
}
