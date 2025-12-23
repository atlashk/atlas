package org.atlas.application.product.port.file.pdf;

import java.util.List;
import org.atlas.application.product.port.file.model.ProductWriteRow;

public interface ProductPdfWriter {

  byte[] write(List<ProductWriteRow> productRows) throws Exception;
}
