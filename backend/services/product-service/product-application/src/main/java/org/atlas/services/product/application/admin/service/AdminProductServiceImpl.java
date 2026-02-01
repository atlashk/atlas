package org.atlas.services.product.application.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.event.DomainEventType;
import org.atlas.libs.framework.domain.common.event.contract.product.ProductEvent;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.ArrayUtil;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.product.application.admin.mapper.AdminProductMapper;
import org.atlas.services.product.port.in.admin.model.AdminCreateProductInput;
import org.atlas.services.product.port.in.admin.model.AdminExportProductInput;
import org.atlas.services.product.port.in.admin.model.AdminImportProductInput;
import org.atlas.services.product.port.in.admin.model.AdminRetrieveProductListInput;
import org.atlas.services.product.port.in.admin.model.AdminUpdateProductInput;
import org.atlas.services.product.application.event.mapper.ProductEventMapper;
import org.atlas.services.product.port.in.admin.service.AdminProductService;
import org.atlas.services.product.port.out.file.csv.ProductCsvReader;
import org.atlas.services.product.port.out.file.csv.ProductCsvWriter;
import org.atlas.services.product.port.out.file.excel.ProductExcelReader;
import org.atlas.services.product.port.out.file.mapper.ProductReadRowMapper;
import org.atlas.services.product.port.out.file.mapper.ProductWriteRowMapper;
import org.atlas.services.product.port.out.file.model.ProductReadRow;
import org.atlas.services.product.port.out.file.model.ProductWriteRow;
import org.atlas.services.product.port.out.file.pdf.ProductPdfWriter;
import org.atlas.services.product.port.out.messaging.ProductEventMessagePublisher;
import org.atlas.services.product.port.out.repository.ProductRepository;
import org.atlas.services.product.port.out.repository.criteria.FindProductCriteria;
import org.atlas.services.product.port.in.front.service.ProductImageService;
import org.atlas.services.product.domain.entity.Product;
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
        .forEach(product -> product.setImage(productImageService.getImage(product.getId())));

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
