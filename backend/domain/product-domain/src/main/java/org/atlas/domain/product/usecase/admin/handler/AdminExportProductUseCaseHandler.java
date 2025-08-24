package org.atlas.domain.product.usecase.admin.handler;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.CategoryEntity;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.port.file.csv.ProductCsvWriterPort;
import org.atlas.domain.product.port.file.excel.ProductExcelWriterPort;
import org.atlas.domain.product.port.file.model.write.ProductRow;
import org.atlas.domain.product.port.file.pdf.ProductPdfWriterPort;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.domain.product.usecase.admin.model.AdminExportProductInput;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.util.CollectionUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminExportProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductCsvWriterPort productCsvWriterPort;
  private final ProductExcelWriterPort productExcelWriterPort;
  private final ProductPdfWriterPort productPdfWriterPort;

  public byte[] handle(AdminExportProductInput input) throws Exception {
    FindProductCriteria criteria = ObjectMapperUtil.getInstance()
        .map(input, FindProductCriteria.class);
    PagingResult<ProductEntity> productEntities = productRepository.findByCriteria(criteria,
        PagingRequest.unpaged());

    // Use custom mapping method for complex attribute mapping
    List<ProductRow> productRows = ObjectMapperUtil.getInstance()
        .mapList(productEntities.getData(), this::toProductRow);

    byte[] fileContent;
    switch (input.getFileType()) {
      case CSV -> fileContent = productCsvWriterPort.write(productRows);
      case EXCEL -> fileContent = productExcelWriterPort.write(productRows);
      case PDF -> fileContent = productPdfWriterPort.write(productRows);
      default -> throw new UnsupportedOperationException(
          "Unsupported file type: " + input.getFileType());
    }
    return fileContent;
  }

  private ProductRow toProductRow(ProductEntity entity) {
    ProductRow row = new ProductRow();

    // Basic info
    row.setId(entity.getId());
    row.setName(entity.getName());
    row.setPrice(entity.getPrice());
    row.setQuantity(entity.getQuantity());
    row.setStatus(entity.getStatus());
    row.setAvailableFrom(entity.getAvailableFrom());
    row.setIsActive(entity.getIsActive());

    // Brand
    if (entity.getBrand() != null) {
      row.setBrandId(entity.getBrand().getId());
    }

    // Categories
    if (CollectionUtil.isNotEmpty(entity.getCategories())) {
      String categoryIds = entity.getCategories().stream()
          .map(CategoryEntity::getId)
          .map(String::valueOf)
          .collect(Collectors.joining("|"));
      row.setCategoryIds(categoryIds);
    }

    return row;
  }
}
