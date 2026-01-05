package org.atlas.product.application.admin.service;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.product.application.admin.mapper.AdminProductMapper;
import org.atlas.product.application.admin.model.AdminCreateProductInput;
import org.atlas.product.application.admin.model.AdminExportProductInput;
import org.atlas.product.application.admin.model.AdminImportProductInput;
import org.atlas.product.application.admin.model.AdminRetrieveProductListInput;
import org.atlas.product.application.admin.model.AdminUpdateProductInput;
import org.atlas.product.application.event.mapper.ProductEventMapper;
import org.atlas.product.application.port.file.csv.ProductCsvReader;
import org.atlas.product.application.port.file.csv.ProductCsvWriter;
import org.atlas.product.application.port.file.excel.ProductExcelReader;
import org.atlas.product.application.port.file.mapper.ProductReadRowMapper;
import org.atlas.product.application.port.file.mapper.ProductWriteRowMapper;
import org.atlas.product.application.port.file.model.ProductReadRow;
import org.atlas.product.application.port.file.model.ProductWriteRow;
import org.atlas.product.application.port.file.pdf.ProductPdfWriter;
import org.atlas.product.application.port.messaging.ProductEventMessagePublisher;
import org.atlas.product.application.port.repository.ProductRepository;
import org.atlas.product.application.port.repository.criteria.FindProductCriteria;
import org.atlas.product.application.service.ProductImageService;
import org.atlas.product.domain.entity.Product;
import org.atlas.common.framework.collection.CollectionUtil;
import org.atlas.common.framework.domain.common.error.DomainError;
import org.atlas.common.framework.domain.common.event.DomainEventType;
import org.atlas.common.framework.domain.common.event.contract.product.ProductEvent;
import org.atlas.common.framework.domain.common.exception.DomainException;
import org.atlas.common.framework.paging.PagingRequest;
import org.atlas.common.framework.paging.PagingResult;
import org.atlas.common.framework.util.ArrayUtil;
import org.atlas.common.framework.util.ObjectMapperUtil;
import org.atlas.common.framework.util.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductServiceImpl implements AdminProductService {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final ProductEventMessagePublisher productEventMessagePublisher;
  private final ProductCsvReader productCsvReader;
  private final ProductExcelReader productExcelReader;
  private final ProductCsvWriter productCsvWriter;
  private final ProductCsvWriter productExcelWriter;
  private final ProductPdfWriter productPdfWriter;

  @Override
  public PagingResult<Product> retrieveProductList(AdminRetrieveProductListInput input) {
    FindProductCriteria criteria = AdminProductMapper.INSTANCE.toFindProductCriteria(input);
    PagingResult<Product> productPage = productRepository.findByCriteria(criteria,
        input.getPagingRequest());

    // Set image
    productPage.getData()
        .forEach(product -> {
          try {
            product.setImage(productImageService.getImage(product.getId()));
          } catch (IOException e) {
            log.error("Failed to get product image: productId={}, error={}",
                product.getId(), e.getMessage());
            product.setImage(StringUtil.EMPTY);
          }
        });

    return productPage;
  }

  @Override
  @Transactional(readOnly = true)
  public Product retrieveProduct(Integer productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
  }

  @Override
  @Transactional(readOnly = true)
  public Long retrieveProductCount() {
    return productRepository.countAll();
  }

  @Override
  @Transactional
  public Integer createProduct(AdminCreateProductInput input) throws Exception {
    Product product = input.getProduct();
    productRepository.insert(product);

    productImageService.uploadImage(product.getId(), input.getImageBytes(),
        input.getImageContentType());

    publishProductCreatedEvent(product);

    return product.getId();
  }

  @Override
  @Transactional
  public void updateProduct(AdminUpdateProductInput input) throws Exception {
    Product product = input.getProduct();
    Product existingProduct = productRepository.findById(product.getId())
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));

    AdminProductMapper.INSTANCE.merge(product, existingProduct);
    productRepository.update(product);

    if (ArrayUtil.isNotEmpty(input.getImageBytes())) {
      productImageService.uploadImage(product.getId(), input.getImageBytes(),
          input.getImageContentType());
    }

    publishProductUpdatedEvent(product);
  }

  @Override
  @Transactional
  public void deleteProduct(Integer productId) {
    // Delete product from DB
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
    productRepository.delete(product.getId());

    // Publish event
    publishProductDeletedEvent(product);
  }

  @Override
  public void importProduct(AdminImportProductInput input) throws Exception {
    // Read rows from file content
    List<ProductReadRow> rows;
    switch (input.getFileType()) {
      case CSV -> rows = productCsvReader.read(input.getFileContent());
      case EXCEL -> rows = productExcelReader.read(input.getFileContent());
      default ->
          throw new UnsupportedOperationException("Unsupported file type: " + input.getFileType());
    }
    if (CollectionUtil.isEmpty(rows)) {
      throw new DomainException(DomainError.NO_IMPORTED_PRODUCT);
    }

    // Sync into DB and publish events
    try {
      List<Product> products = rows.stream()
          .map(ProductReadRowMapper.INSTANCE::toProduct)
          .toList();
      productRepository.insertBatch(products);
      products.forEach(this::publishProductCreatedEvent);
      log.info("Imported {} products", rows.size());
    } catch (Exception e) {
      throw new DomainException(DomainError.FAILED_TO_IMPORT_PRODUCT, e.getMessage());
    }
  }

  @Override
  public byte[] exportProduct(AdminExportProductInput input) throws Exception {
    FindProductCriteria criteria = AdminProductMapper.INSTANCE.toFindProductCriteria(input);
    PagingResult<Product> products = productRepository.findByCriteria(criteria,
        PagingRequest.unpaged());

    // Use custom mapping method for complex attribute mapping
    List<ProductWriteRow> productRows = ObjectMapperUtil.mapList(products.getData(),
        ProductWriteRowMapper.INSTANCE::toProductWriteRow);

    byte[] fileContent;
    switch (input.getFileType()) {
      case CSV -> fileContent = productCsvWriter.write(productRows);
      case EXCEL -> fileContent = productExcelWriter.write(productRows);
      case PDF -> fileContent = productPdfWriter.write(productRows);
      default -> throw new UnsupportedOperationException(
          "Unsupported file type: " + input.getFileType());
    }
    return fileContent;
  }

  private void publishProductCreatedEvent(Product product) {
    ProductEvent event = new ProductEvent(DomainEventType.PRODUCT_CREATED);
    ProductEventMapper.INSTANCE.merge(product, event);
    productEventMessagePublisher.publish(event);
  }

  private void publishProductUpdatedEvent(Product product) {
    ProductEvent event = new ProductEvent(DomainEventType.PRODUCT_UPDATED);
    ProductEventMapper.INSTANCE.merge(product, event);
    productEventMessagePublisher.publish(event);
  }

  private void publishProductDeletedEvent(Product product) {
    ProductEvent event = new ProductEvent(DomainEventType.PRODUCT_DELETED);
    ProductEventMapper.INSTANCE.merge(product, event);
    productEventMessagePublisher.publish(event);
  }
}
