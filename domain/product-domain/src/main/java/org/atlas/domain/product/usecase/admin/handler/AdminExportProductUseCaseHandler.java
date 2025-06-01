package org.atlas.domain.product.usecase.admin.handler;

import java.util.List;

import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.port.file.csv.ProductCsvWriterPort;
import org.atlas.domain.product.port.file.excel.ProductExcelWriterPort;
import org.atlas.domain.product.port.file.model.write.ProductRow;
import org.atlas.domain.product.repository.FindProductCriteria;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.usecase.admin.model.AdminExportProductInput;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;

import lombok.RequiredArgsConstructor;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminExportProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductCsvWriterPort productCsvWriterPort;
  private final ProductExcelWriterPort productExcelWriterPort;

  public byte[] handle(AdminExportProductInput input) throws Exception {
    FindProductCriteria criteria = ObjectMapperUtil.getInstance()
        .map(input, FindProductCriteria.class);
    PagingResult<ProductEntity> productEntities = productRepository.findByCriteria(criteria,
        PagingRequest.unpaged());
    List<ProductRow> productRows = ObjectMapperUtil.getInstance().mapList(
        productEntities.getData(), ProductRow.class);

    byte[] fileContent;
    switch (input.getFileType()) {
      case CSV -> fileContent = productCsvWriterPort.write(productRows);
      case EXCEL -> fileContent = productExcelWriterPort.write(productRows);
      default -> throw new UnsupportedOperationException(
          "Unsupported file type: " + input.getFileType());
    }
    return fileContent;
  }
}
