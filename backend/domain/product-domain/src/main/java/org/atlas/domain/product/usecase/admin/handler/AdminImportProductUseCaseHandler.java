package org.atlas.domain.product.usecase.admin.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.infrastructure.file.csv.ProductCsvReader;
import org.atlas.domain.product.infrastructure.file.excel.ProductExcelReader;
import org.atlas.domain.product.infrastructure.file.model.read.ProductRow;
import org.atlas.domain.product.infrastructure.messaging.ProductEventMessagePublisher;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.usecase.admin.mapper.AdminProductMapper;
import org.atlas.domain.product.usecase.admin.model.AdminImportProductInput;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.event.contract.product.ProductCreatedEvent;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
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
      List<Product> products = rows.stream()
          .map(this::toProduct)
          .toList();
      productRepository.insertBatch(products);
      products.forEach(this::publishEvent);
      log.info("Imported {} products", rows.size());
      return null;
    } catch (Exception e) {
      throw new DomainException(DomainError.FAILED_TO_IMPORT_PRODUCT, e.getMessage());
    }
  }

  private Product toProduct(ProductRow row) {
    return AdminProductMapper.INSTANCE.toProduct(row);
  }

  private void publishEvent(Product product) {
    org.atlas.framework.domain.event.contract.product.model.Product productPayload =
        AdminProductMapper.INSTANCE.toProduct(product);
    ProductCreatedEvent event = new ProductCreatedEvent(productPayload);
    productEventMessagePublisher.publish(event);
  }
}
