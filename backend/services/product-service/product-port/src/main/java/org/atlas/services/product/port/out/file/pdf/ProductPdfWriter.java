package org.atlas.services.product.port.out.file.pdf;

import java.util.List;
import org.atlas.services.product.port.out.file.model.ProductWriteRow;

public interface ProductPdfWriter {

  byte[] write(List<ProductWriteRow> productRows) throws Exception;
}
