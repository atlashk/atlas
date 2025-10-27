package org.atlas.domain.product.usecase.admin.handler;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.atlas.domain.product.entity.BrandEntity;
import org.atlas.domain.product.entity.CategoryEntity;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.infrastructure.file.csv.ProductCsvReader;
import org.atlas.domain.product.infrastructure.file.excel.ProductExcelReader;
import org.atlas.domain.product.infrastructure.file.model.read.ProductRow;
import org.atlas.domain.product.infrastructure.messaging.ProductEventMessagePublisher;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.usecase.admin.model.AdminImportProductInput;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.event.contract.product.ProductCreatedEvent;
import org.atlas.framework.domain.event.contract.product.model.Product;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.framework.util.CollectionUtil;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class AdminImportProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductCsvReader productCsvReader;
  private final ProductExcelReader productExcelReader;
  private final ProductEventMessagePublisher productEventMessagePublisher;

  public Void handle(AdminImportProductInput input) throws Exception {
    // Read rows from file content
    List<ProductRow> rows;
    switch (input.getFileType()) {
      case CSV -> rows = productCsvReader.read(input.getFileContent());
      case EXCEL -> rows = productExcelReader.read(input.getFileContent());
      default -> throw new UnsupportedOperationException(
          "Unsupported file type: " + input.getFileType());
    }
    if (CollectionUtil.isEmpty(rows)) {
      throw new DomainException(DomainError.NO_IMPORTED_PRODUCT);
    }

    // Sync into DB and publish events
    try {
      List<ProductEntity> products = rows.stream()
          .map(this::toProductEntity)
          .toList();
      productRepository.insertBatch(products);
      products.forEach(this::publishEvent);
      log.info("Imported {} products", rows.size());
      return null;
    } catch (Exception e) {
      throw new DomainException(DomainError.FAILED_TO_IMPORT_PRODUCT, e.getMessage());
    }
  }

  private ProductEntity toProductEntity(ProductRow row) {
    // Product
    ProductEntity product = ObjectMapperUtil.getInstance().map(row, ProductEntity.class);

    // Brand
    BrandEntity brandEntity = new BrandEntity();
    brandEntity.setId(row.getBrandId());
    product.setBrand(brandEntity);

    // Categories
    List<CategoryEntity> categoryEntities = Arrays.stream(row.getCategoryIds().split("\\|"))
        .filter(StringUtils::isNotBlank)
        .map(categoryIdStr -> {
          CategoryEntity categoryEntity = new CategoryEntity();
          categoryEntity.setId(Integer.parseInt(categoryIdStr.trim()));
          return categoryEntity;
        })
        .toList();
    product.setCategories(categoryEntities);

    return product;
  }

  private void publishEvent(ProductEntity product) {
    Product productPayload = ObjectMapperUtil.getInstance().map(product, Product.class);
    ProductCreatedEvent event = new ProductCreatedEvent(productPayload);
    productEventMessagePublisher.publish(event);
  }
}
