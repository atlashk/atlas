package org.atlas.services.catalog.port.out.file.pdf;

import java.util.List;
import org.atlas.services.catalog.port.out.file.model.ProductWriteRow;

public interface ProductPdfWriter {

  byte[] write(List<ProductWriteRow> productRows) throws Exception;
}
