package org.atlas.services.product.application.port.file.pdf;

import java.util.List;
import org.atlas.services.product.application.port.file.model.ProductWriteRow;

public interface ProductPdfWriter {

  byte[] write(List<ProductWriteRow> productRows) throws Exception;
}
