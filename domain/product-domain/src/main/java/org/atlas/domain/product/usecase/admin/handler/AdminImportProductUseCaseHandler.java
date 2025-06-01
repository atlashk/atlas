package org.atlas.domain.product.usecase.admin.handler;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.atlas.domain.product.entity.BrandEntity;
import org.atlas.domain.product.entity.CategoryEntity;
import org.atlas.domain.product.entity.ProductAttributeEntity;
import org.atlas.domain.product.entity.ProductDetailsEntity;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.port.file.csv.ProductCsvReaderPort;
import org.atlas.domain.product.port.file.excel.ProductExcelReaderPort;
import org.atlas.domain.product.port.file.model.read.ProductRow;
import org.atlas.domain.product.port.messaging.ProductMessagePublisherPort;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.usecase.admin.model.AdminImportProductInput;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.contract.product.ProductCreatedEvent;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.error.AppError;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.transaction.TransactionPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class AdminImportProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ApplicationConfigPort applicationConfigPort;
  private final ProductCsvReaderPort productCsvReaderPort;
  private final ProductExcelReaderPort productExcelReaderPort;
  private final ProductMessagePublisherPort productMessagePublisherPort;
  private final TransactionPort transactionPort;

  public Void handle(AdminImportProductInput input) throws Exception {
    // Read rows from file content
    List<ProductRow> rows;
    switch (input.getFileType()) {
      case CSV -> rows = productCsvReaderPort.read(input.getFileContent());
      case EXCEL -> rows = productExcelReaderPort.read(input.getFileContent());
      default -> throw new UnsupportedOperationException(
          "Unsupported file type: " + input.getFileType());
    }
    if (CollectionUtils.isEmpty(rows)) {
      throw new DomainException(AppError.NO_IMPORTED_PRODUCT);
    }

    // Sync into DB and publish events
    try {
      transactionPort.execute(() -> {
        List<ProductEntity> productEntities = rows.stream()
            .map(this::newProductEntity)
            .toList();
        productRepository.insertBatch(productEntities);
        productEntities.forEach(productEntity -> {
          ProductCreatedEvent event = new ProductCreatedEvent(
              applicationConfigPort.getApplicationName());
          ObjectMapperUtil.getInstance()
              .merge(productEntity, event);
          event.setProductId(productEntity.getId());
          productMessagePublisherPort.publish(event);
        });
      });
      log.info("Imported {} products", rows.size());
      return null;
    } catch (Exception e) {
      throw new DomainException(AppError.FAILED_TO_IMPORT_PRODUCT, e.getMessage());
    }
  }

  private ProductEntity newProductEntity(ProductRow row) {
    // Product
    ProductEntity productEntity = ObjectMapperUtil.getInstance().map(row, ProductEntity.class);

    // Product detail
    ProductDetailsEntity productDetailsEntity = new ProductDetailsEntity();
    productDetailsEntity.setDescription(row.getDescription());
    productEntity.setDetails(productDetailsEntity);

    // Product attributes
    ProductAttributeEntity productAttributeEntity1 = new ProductAttributeEntity();
    productAttributeEntity1.setName(row.getAttributeName1());
    productAttributeEntity1.setValue(row.getAttributeValue1());
    productEntity.addAttribute(productAttributeEntity1);

    ProductAttributeEntity productAttributeEntity2 = new ProductAttributeEntity();
    productAttributeEntity2.setName(row.getAttributeName2());
    productAttributeEntity2.setValue(row.getAttributeValue2());
    productEntity.addAttribute(productAttributeEntity2);

    ProductAttributeEntity productAttributeEntity3 = new ProductAttributeEntity();
    productAttributeEntity3.setName(row.getAttributeName3());
    productAttributeEntity3.setValue(row.getAttributeValue3());
    productEntity.addAttribute(productAttributeEntity3);

    // Brand
    BrandEntity brandEntity = new BrandEntity();
    brandEntity.setId(row.getBrandId());
    productEntity.setBrand(brandEntity);

    // Categories
    List<CategoryEntity> categoryEntities = row.getCategoryIds()
        .stream()
        .map(categoryId -> {
          CategoryEntity categoryEntity = new CategoryEntity();
          categoryEntity.setId(categoryId);
          return categoryEntity;
        })
        .toList();
    productEntity.setCategories(categoryEntities);

    return productEntity;
  }
}
