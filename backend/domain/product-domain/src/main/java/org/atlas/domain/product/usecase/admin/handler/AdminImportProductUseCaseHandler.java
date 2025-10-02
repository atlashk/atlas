package org.atlas.domain.product.usecase.admin.handler;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.atlas.domain.product.entity.BrandEntity;
import org.atlas.domain.product.entity.CategoryEntity;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.port.file.csv.ProductCsvReaderPort;
import org.atlas.domain.product.port.file.excel.ProductExcelReaderPort;
import org.atlas.domain.product.port.file.model.read.ProductRow;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.usecase.admin.model.AdminImportProductInput;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.contract.product.ProductCreatedEvent;
import org.atlas.framework.domain.event.contract.product.model.Product;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.messaging.publisher.MessagePublisherPort;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.util.CollectionUtil;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class AdminImportProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final MessagePublisherPort messagePublisherPort;
  private final ProductCsvReaderPort productCsvReaderPort;
  private final ProductExcelReaderPort productExcelReaderPort;

  public Void handle(AdminImportProductInput input) throws Exception {
    // Read rows from file content
    List<ProductRow> rows;
    switch (input.getFileType()) {
      case CSV -> rows = productCsvReaderPort.read(input.getFileContent());
      case EXCEL -> rows = productExcelReaderPort.read(input.getFileContent());
      default -> throw new UnsupportedOperationException(
          "Unsupported file type: " + input.getFileType());
    }
    if (CollectionUtil.isEmpty(rows)) {
      throw new DomainException(DomainError.NO_IMPORTED_PRODUCT);
    }

    // Sync into DB and publish events
    try {
      List<ProductEntity> productEntities = rows.stream()
          .map(this::toProductEntity)
          .toList();
      productRepository.insertBatch(productEntities);
      productEntities.forEach(productEntity -> {
        Product product = ObjectMapperUtil.getInstance().map(productEntity, Product.class);
        ProductCreatedEvent event = new ProductCreatedEvent(
            applicationConfigPort.getApplicationName(), product);
        messagePublisherPort.publish(event);
      });
      log.info("Imported {} products", rows.size());
      return null;
    } catch (Exception e) {
      throw new DomainException(DomainError.FAILED_TO_IMPORT_PRODUCT, e.getMessage());
    }
  }

  private ProductEntity toProductEntity(ProductRow row) {
    // Product
    ProductEntity productEntity = ObjectMapperUtil.getInstance().map(row, ProductEntity.class);

    // Brand
    BrandEntity brandEntity = new BrandEntity();
    brandEntity.setId(row.getBrandId());
    productEntity.setBrand(brandEntity);

    // Categories
    List<CategoryEntity> categoryEntities = Arrays.stream(row.getCategoryIds().split("\\|"))
        .filter(StringUtils::isNotBlank)
        .map(categoryIdStr -> {
          CategoryEntity categoryEntity = new CategoryEntity();
          categoryEntity.setId(Integer.parseInt(categoryIdStr.trim()));
          return categoryEntity;
        })
        .toList();
    productEntity.setCategories(categoryEntities);

    return productEntity;
  }
}
