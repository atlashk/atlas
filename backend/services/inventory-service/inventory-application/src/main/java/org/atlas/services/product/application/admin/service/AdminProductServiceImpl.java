package org.atlas.services.product.application.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.event.DomainEventType;
import org.atlas.libs.framework.domain.common.event.contract.product.ProductCreatedEvent;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.libs.framework.util.ArrayUtil;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.product.application.admin.mapper.AdminProductMapper;
import org.atlas.services.product.application.event.mapper.ProductEventMapper;
import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.product.port.in.admin.model.AdminCreateProductInput;
import org.atlas.services.product.port.in.admin.model.AdminExportProductInput;
import org.atlas.services.product.port.in.admin.model.AdminImportProductInput;
import org.atlas.services.product.port.in.admin.model.AdminRetrieveProductListInput;
import org.atlas.services.product.port.in.admin.model.AdminUpdateProductInput;
import org.atlas.services.product.port.in.admin.service.AdminProductService;
import org.atlas.services.product.port.in.service.ProductImageService;
import org.atlas.services.product.port.out.file.csv.ProductCsvReader;
import org.atlas.services.product.port.out.file.csv.ProductCsvWriter;
import org.atlas.services.product.port.out.file.excel.ProductExcelReader;
import org.atlas.services.product.port.out.file.mapper.ProductReadRowMapper;
import org.atlas.services.product.port.out.file.mapper.ProductWriteRowMapper;
import org.atlas.services.product.port.out.file.model.ProductReadRow;
import org.atlas.services.product.port.out.file.model.ProductWriteRow;
import org.atlas.services.product.port.out.file.pdf.ProductPdfWriter;
import org.atlas.services.inventory.port.out.messaging.StockEventMessagePublisher;
import org.atlas.services.inventory.port.out.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductServiceImpl implements AdminProductService {

  private final StockRepository stockRepository;
  private final ProductImageService productImageService;
  private final SequenceGenerator sequenceGenerator;
  private final StockEventMessagePublisher stockEventMessagePublisher;
  private final ProductCsvReader productCsvReader;
  private final ProductExcelReader productExcelReader;
  private final ProductCsvWriter productCsvWriter;
  private final ProductCsvWriter productExcelWriter;
  private final ProductPdfWriter productPdfWriter;

  @Override
  public PagingResult<StockEntity> retrieveProductList(AdminRetrieveProductListInput input) {
    StockRepository.FindProductCriteria criteria = AdminProductMapper.INSTANCE.toFindProductCriteria(
        input);
    PagingResult<StockEntity> productPage = stockRepository.findByCriteria(criteria,
        input.getPagingRequest());

    // Set image
    productPage.getData()
        .forEach(product -> product.setImage(productImageService.getImage(product.getId())));

    return productPage;
  }

  @Override
  @Transactional(readOnly = true)
  public StockEntity retrieveProduct(String id) {
    return stockRepository.findById(id)
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
  }

  @Override
  @Transactional(readOnly = true)
  public Long retrieveProductCount() {
    return stockRepository.countAll();
  }

  @Override
  @Transactional
  public String createProduct(AdminCreateProductInput input) throws Exception {
    StockEntity product = input.getProduct();
    product.setId(sequenceGenerator.generate(SequenceType.PRODUCT));
    stockRepository.insert(product);

    productImageService.uploadImage(product.getId(), input.getImageBytes(),
        input.getImageContentType());

    publishProductCreatedEvent(product);

    return product.getId();
  }

  @Override
  @Transactional
  public void updateProduct(AdminUpdateProductInput input) throws Exception {
    StockEntity product = input.getProduct();
    StockEntity existingProduct = stockRepository.findById(product.getId())
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));

    AdminProductMapper.INSTANCE.merge(product, existingProduct);
    stockRepository.update(product);

    if (ArrayUtil.isNotEmpty(input.getImageBytes())) {
      productImageService.uploadImage(product.getId(), input.getImageBytes(),
          input.getImageContentType());
    }

    publishProductUpdatedEvent(product);
  }

  @Override
  @Transactional
  public void deleteProduct(String id) {
    // Delete product from DB
    StockEntity product = stockRepository.findById(id)
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
    stockRepository.deleteById(product.getId());

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
      List<StockEntity> products = rows.stream()
          .map(row -> {
            StockEntity product = ProductReadRowMapper.INSTANCE.toProduct(row);
            product.setId(sequenceGenerator.generate(SequenceType.PRODUCT));
            return product;
          })
          .toList();
      stockRepository.insertBatch(products);
      products.forEach(this::publishProductCreatedEvent);
      log.info("Imported {} products", rows.size());
    } catch (Exception e) {
      throw new DomainException(DomainError.FAILED_TO_IMPORT_PRODUCT, e.getMessage());
    }
  }

  @Override
  public byte[] exportProduct(AdminExportProductInput input) throws Exception {
    StockRepository.FindProductCriteria criteria = AdminProductMapper.INSTANCE
        .toFindProductCriteria(input);
    PagingResult<StockEntity> products = stockRepository.findByCriteria(criteria,
        PagingRequest.unpaged());

    // Use custom mapping method for complex attribute mapping
    List<ProductWriteRow> productRows = MapperUtil.mapList(products.getData(),
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

  private void publishProductCreatedEvent(StockEntity product) {
    ProductCreatedEvent event = new ProductCreatedEvent(DomainEventType.PRODUCT_CREATED);
    ProductEventMapper.INSTANCE.merge(product, event);
    stockEventMessagePublisher.publish(event);
  }

  private void publishProductUpdatedEvent(StockEntity product) {
    ProductCreatedEvent event = new ProductCreatedEvent(DomainEventType.PRODUCT_UPDATED);
    ProductEventMapper.INSTANCE.merge(product, event);
    stockEventMessagePublisher.publish(event);
  }

  private void publishProductDeletedEvent(StockEntity product) {
    ProductCreatedEvent event = new ProductCreatedEvent(DomainEventType.PRODUCT_DELETED);
    ProductEventMapper.INSTANCE.merge(product, event);
    stockEventMessagePublisher.publish(event);
  }
}
