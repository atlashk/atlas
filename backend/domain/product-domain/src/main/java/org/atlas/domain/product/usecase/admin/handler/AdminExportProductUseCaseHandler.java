package org.atlas.domain.product.usecase.admin.handler;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.Category;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.infrastructure.file.csv.ProductCsvWriter;
import org.atlas.domain.product.infrastructure.file.excel.ProductExcelWriter;
import org.atlas.domain.product.infrastructure.file.model.write.ProductRow;
import org.atlas.domain.product.infrastructure.file.pdf.ProductPdfWriter;
import org.atlas.domain.product.repository.ProductRepository;
import org.atlas.domain.product.repository.criteria.FindProductCriteria;
import org.atlas.domain.product.usecase.admin.mapper.AdminProductMapper;
import org.atlas.domain.product.usecase.admin.model.AdminExportProductInput;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.util.CollectionUtil;
import org.atlas.framework.util.ObjectMapperUtil;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class AdminExportProductUseCaseHandler {

  private final ProductRepository productRepository;
  private final ProductCsvWriter productCsvWriter;
  private final ProductExcelWriter productExcelWriter;
  private final ProductPdfWriter productPdfWriter;

  public byte[] handle(AdminExportProductInput input) throws Exception {
    FindProductCriteria criteria = AdminProductMapper.INSTANCE.toFindProductCriteria(input);
    PagingResult<Product> products = productRepository.findByCriteria(criteria,
        PagingRequest.unpaged());

    // Use custom mapping method for complex attribute mapping
    List<ProductRow> productRows = ObjectMapperUtil.mapList(products.getData(),
        AdminProductMapper.INSTANCE::toProductRow);

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

  private ProductRow toProductRow(Product entity) {
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
          .map(Category::getId)
          .map(String::valueOf)
          .collect(Collectors.joining("|"));
      row.setCategoryIds(categoryIds);
    }

    return row;
  }
}
