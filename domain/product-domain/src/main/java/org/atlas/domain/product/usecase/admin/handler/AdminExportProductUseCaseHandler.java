package org.atlas.domain.product.usecase.admin.handler;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.atlas.domain.product.entity.CategoryEntity;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.entity.ProductAttributeEntity;
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
    
    // Use custom mapping method for complex attribute mapping
    List<ProductRow> productRows = ObjectMapperUtil.getInstance()
        .mapList(productEntities.getData(), this::toProductRow);

    byte[] fileContent;
    switch (input.getFileType()) {
      case CSV -> fileContent = productCsvWriterPort.write(productRows);
      case EXCEL -> fileContent = productExcelWriterPort.write(productRows);
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

    // Product details
    if (entity.getDetails() != null) {
      row.setDescription(entity.getDetails().getDescription());
    }

    // Brand
    if (entity.getBrand() != null) {
      row.setBrandId(entity.getBrand().getId());
    }

    // Categories
    if (entity.getCategories() != null && !entity.getCategories().isEmpty()) {
      String categoryIds = entity.getCategories().stream()
          .map(CategoryEntity::getId)
          .map(String::valueOf)
          .collect(Collectors.joining("|"));
      row.setCategoryIds(categoryIds);
    }

    // Attributes - map up to 3 attributes to specific columns
    if (CollectionUtils.isNotEmpty(entity.getAttributes())) {
      List<ProductAttributeEntity> attributes = entity.getAttributes()
          .stream()
          .limit(3)
          .toList();

      for (int i = 0; i < attributes.size(); i++) {
        ProductAttributeEntity attr = attributes.get(i);
        switch (i) {
          case 0:
            row.setAttributeName1(attr.getName());
            row.setAttributeValue1(attr.getValue());
            break;
          case 1:
            row.setAttributeName2(attr.getName());
            row.setAttributeValue2(attr.getValue());
            break;
          case 2:
            row.setAttributeName3(attr.getName());
            row.setAttributeValue3(attr.getValue());
            break;
        }
      }
    }
    
    return row;
  }
}
