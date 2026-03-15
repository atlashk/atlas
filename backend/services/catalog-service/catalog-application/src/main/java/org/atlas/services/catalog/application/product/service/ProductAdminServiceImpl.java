package org.atlas.services.catalog.application.product.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cache.CacheService;
import org.atlas.libs.framework.domain.event.contract.catalog.ProductCreatedEvent;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.security.authorization.RequiredAdmin;
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.libs.framework.util.ArrayUtil;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.application.product.constant.ProductConstant;
import org.atlas.services.catalog.application.product.mapper.ProductAdminMapper;
import org.atlas.services.catalog.application.product.mapper.ProductEventMapper;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.domain.error.DomainError;
import org.atlas.services.catalog.domain.exception.DomainException;
import org.atlas.services.catalog.port.in.product.model.admin.CreateProductInput;
import org.atlas.services.catalog.port.in.product.model.admin.ExportProductInput;
import org.atlas.services.catalog.port.in.product.model.admin.ImportProductInput;
import org.atlas.services.catalog.port.in.product.model.admin.RetrieveProductListInput;
import org.atlas.services.catalog.port.in.product.model.admin.UpdateProductInput;
import org.atlas.services.catalog.port.in.product.service.ProductAdminService;
import org.atlas.services.catalog.port.in.product.service.ProductImageService;
import org.atlas.services.catalog.port.out.file.csv.ProductCsvReader;
import org.atlas.services.catalog.port.out.file.csv.ProductCsvWriter;
import org.atlas.services.catalog.port.out.file.excel.ProductExcelReader;
import org.atlas.services.catalog.port.out.file.mapper.ProductReadRowMapper;
import org.atlas.services.catalog.port.out.file.mapper.ProductWriteRowMapper;
import org.atlas.services.catalog.port.out.file.model.ProductReadRow;
import org.atlas.services.catalog.port.out.file.model.ProductWriteRow;
import org.atlas.services.catalog.port.out.file.pdf.ProductPdfWriter;
import org.atlas.services.catalog.port.out.search.SearchService;
import org.atlas.services.catalog.port.out.messaging.ProductEventMessagePublisher;
import org.atlas.services.catalog.port.out.repository.ProductRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredAdmin
@RequiredArgsConstructor
@Slf4j
public class ProductAdminServiceImpl implements ProductAdminService {

  private final ProductRepository productRepository;
  private final ProductImageService productImageService;
  private final SequenceGenerator sequenceGenerator;
  private final ProductEventMessagePublisher productEventMessagePublisher;
  private final ObjectProvider<SearchService> searchServiceProvider;
  private final CacheService cacheService;
  private final ProductCsvReader productCsvReader;
  private final ProductExcelReader productExcelReader;
  private final ProductCsvWriter productCsvWriter;
  private final ProductCsvWriter productExcelWriter;
  private final ProductPdfWriter productPdfWriter;

  @Override
  public PagingResult<ProductEntity> retrieveProductList(RetrieveProductListInput input) {
    ProductRepository.FindProductCriteria criteria = ProductAdminMapper.INSTANCE.toFindProductCriteria(
        input);
    PagingResult<ProductEntity> productPage = productRepository.findByCriteria(criteria,
        input.getPagingRequest());

    // Set image
    productPage.getData()
        .forEach(product -> product.setImage(productImageService.getImage(product.getId())));

    return productPage;
  }

  @Override
  @Transactional(readOnly = true)
  public ProductEntity retrieveProduct(String id) {
    ProductEntity product = productRepository.findById(id)
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));

    // Set image
    product.setImage(productImageService.getImage(product.getId()));

    return product;
  }

  @Override
  @Transactional
  public String createProduct(CreateProductInput input) throws Exception {
    log.info("Creating product: name={}", input.getProduct().getName());

    // Save product to DB
    ProductEntity product = input.getProduct();
    product.setId(sequenceGenerator.generate(SequenceType.PRODUCT));
    productRepository.insert(product);
    log.debug("Product saved to database: id={}", product.getId());

    // Publish event
    ProductCreatedEvent event = new ProductCreatedEvent();
    ProductEventMapper.INSTANCE.merge(product, event);
    productEventMessagePublisher.publish(event);
    log.debug("ProductCreatedEvent published for product: id={}", product.getId());

    // Upload product image
    productImageService.uploadImage(product.getId(), input.getImageBytes(),
        input.getImageContentType());
    log.debug("Product image uploaded: id={}", product.getId());

    // Create search document (if search service is available)
    SearchService searchService = searchServiceProvider.getIfAvailable();
    if (searchService != null) {
      searchService.save(product);
      log.debug("Search document created for product: id={}", product.getId());
    }

    log.info("Product created successfully: id={}", product.getId());
    return product.getId();
  }

  @Override
  @Transactional
  public void updateProduct(UpdateProductInput input) throws Exception {
    log.info("Updating product: id={}", input.getProduct().getId());

    // Update product in DB
    ProductEntity product = input.getProduct();
    ProductEntity existingProduct = productRepository.findById(product.getId())
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
    ProductAdminMapper.INSTANCE.merge(product, existingProduct);
    productRepository.update(product);
    log.debug("Product updated in database: id={}", product.getId());

    // Upload new product image if provided
    if (ArrayUtil.isNotEmpty(input.getImageBytes())) {
      productImageService.uploadImage(product.getId(), input.getImageBytes(),
          input.getImageContentType());
      log.debug("Product image updated: id={}", product.getId());
    }

    // Update search document (if search service is available)
    SearchService searchService = searchServiceProvider.getIfAvailable();
    if (searchService != null) {
      searchService.save(product);
      log.debug("Search document updated for product: id={}", product.getId());
    }

    // Evict product cache
    cacheService.evict(ProductConstant.PRODUCT_CACHE, product.getId());
    log.debug("Product cache evicted: id={}", product.getId());

    log.info("Product updated successfully: id={}", product.getId());
  }

  @Override
  @Transactional
  public void deleteProduct(String id) {
    log.info("Deleting product: id={}", id);

    // Delete product from DB
    ProductEntity product = productRepository.findById(id)
        .orElseThrow(() -> new DomainException(DomainError.PRODUCT_NOT_FOUND));
    productRepository.deleteById(product.getId());
    log.debug("Product deleted from database: id={}", product.getId());

    // Publish event
    ProductCreatedEvent event = new ProductCreatedEvent();
    ProductEventMapper.INSTANCE.merge(product, event);
    productEventMessagePublisher.publish(event);
    log.debug("ProductDeletedEvent published for product: id={}", product.getId());

    // Delete product image
    productImageService.deleteImage(product.getId());
    log.debug("Product image deleted: id={}", product.getId());

    // Delete search document (if search service is available)
    SearchService searchService = searchServiceProvider.getIfAvailable();
    if (searchService != null) {
      searchService.delete(product.getId());
      log.debug("Search document deleted for product: id={}", product.getId());
    }

    // Evict product cache
    cacheService.evict(ProductConstant.PRODUCT_CACHE, product.getId());
    log.debug("Product cache evicted: id={}", product.getId());

    log.info("Product deleted successfully: id={}", id);
  }

  @Override
  @Transactional
  public void importProduct(ImportProductInput input) throws Exception {
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

    try {
      // Save products to DB in batch
      List<ProductEntity> products = rows.stream().map(row -> {
        ProductEntity product = ProductReadRowMapper.INSTANCE.toProduct(row);
        product.setId(sequenceGenerator.generate(SequenceType.PRODUCT));
        return product;
      }).toList();
      productRepository.insertAll(products);

      // Publish events for each created product
      products.forEach(product -> {
        ProductCreatedEvent event = new ProductCreatedEvent();
        ProductEventMapper.INSTANCE.merge(product, event);
        productEventMessagePublisher.publish(event);
      });
      log.info("Imported {} products", rows.size());
    } catch (Exception e) {
      throw new DomainException(DomainError.FAILED_TO_IMPORT_PRODUCT, e.getMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public byte[] exportProduct(ExportProductInput input) throws Exception {
    ProductRepository.FindProductCriteria criteria = ProductAdminMapper.INSTANCE.toFindProductCriteria(
        input);
    PagingResult<ProductEntity> products = productRepository.findByCriteria(criteria,
        PagingRequest.unpaged());

    // Use custom mapping method for complex attribute mapping
    List<ProductWriteRow> productRows = MapperUtil.mapList(products.getData(),
        ProductWriteRowMapper.INSTANCE::toProductWriteRow);

    byte[] fileContent;
    switch (input.getFileType()) {
      case CSV -> fileContent = productCsvWriter.write(productRows);
      case EXCEL -> fileContent = productExcelWriter.write(productRows);
      case PDF -> fileContent = productPdfWriter.write(productRows);
      default ->
          throw new UnsupportedOperationException("Unsupported file type: " + input.getFileType());
    }
    return fileContent;
  }

  @Override
  @Transactional(readOnly = true)
  public Long retrieveTotalProductCount() {
    return productRepository.countAll();
  }
}
