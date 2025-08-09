package org.atlas.infrastructure.api.server.rest.adapter.product.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.shared.enums.ProductStatus;
import org.atlas.domain.product.usecase.admin.handler.AdminCreateProductUseCaseHandler;
import org.atlas.domain.product.usecase.admin.handler.AdminDeleteProductUseCaseHandler;
import org.atlas.domain.product.usecase.admin.handler.AdminExportProductUseCaseHandler;
import org.atlas.domain.product.usecase.admin.handler.AdminGetProductUseCaseHandler;
import org.atlas.domain.product.usecase.admin.handler.AdminImportProductUseCaseHandler;
import org.atlas.domain.product.usecase.admin.handler.AdminListProductUseCaseHandler;
import org.atlas.domain.product.usecase.admin.handler.AdminUpdateProductUseCaseHandler;
import org.atlas.domain.product.usecase.admin.model.AdminExportProductInput;
import org.atlas.domain.product.usecase.admin.model.AdminImportProductInput;
import org.atlas.domain.product.usecase.admin.model.AdminListProductInput;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.file.enums.FileType;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.util.DateUtil;
import org.atlas.infrastructure.api.server.rest.adapter.product.admin.mapper.AdminProductMapper;
import org.atlas.infrastructure.api.server.rest.adapter.product.admin.model.AdminCreateProductRequest;
import org.atlas.infrastructure.api.server.rest.adapter.product.admin.model.AdminUpdateProductRequest;
import org.atlas.infrastructure.api.server.rest.adapter.product.shared.ProductResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/admin/products")
@Validated
@RequiredArgsConstructor
public class AdminProductController {

  private final AdminListProductUseCaseHandler adminListProductUseCaseHandler;
  private final AdminGetProductUseCaseHandler adminGetProductUseCaseHandler;
  private final AdminCreateProductUseCaseHandler adminCreateProductUseCaseHandler;
  private final AdminUpdateProductUseCaseHandler adminUpdateProductUseCaseHandler;
  private final AdminDeleteProductUseCaseHandler adminDeleteProductUseCaseHandler;
  private final AdminImportProductUseCaseHandler adminImportProductUseCaseHandler;
  private final AdminExportProductUseCaseHandler adminExportProductUseCaseHandler;

  @Operation(summary = "List products with optional filters and pagination.")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<List<ProductResponse>> listProduct(
      @Parameter(description = "The unique identifier of the product.", example = "1")
      @RequestParam(name = "id", required = false) Integer id,
      @Parameter(description = "Keyword for searching products.", example = "T-Shirt")
      @RequestParam(name = "keyword", required = false) String keyword,
      @Parameter(description = "Minimum price for filtering products.", example = "10.00")
      @RequestParam(name = "min_price", required = false) BigDecimal minPrice,
      @Parameter(description = "Maximum price for filtering products.", example = "100.00")
      @RequestParam(name = "max_price", required = false) BigDecimal maxPrice,
      @Parameter(description = "Status of the product.", example = "IN_STOCK")
      @RequestParam(name = "status", required = false) ProductStatus status,
      @Parameter(description = "Date from which the product is available (ISO 8601 format).", example = "2023-01-01T00:00:00Z")
      @RequestParam(name = "available_from", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date availableFrom,
      @Parameter(description = "Indicates if the product is active.", example = "true")
      @RequestParam(name = "is_active", required = false) Boolean isActive,
      @Parameter(description = "Brand ID for filtering products.", example = "1")
      @RequestParam(name = "brand_id", required = false) Integer brandId,
      @Parameter(description = "List of category IDs for filtering products.", example = "[1, 2, 3]")
      @RequestParam(name = "category_ids", required = false) List<Integer> categoryIds,
      @Parameter(description = "Page number for pagination.", example = "1")
      @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
      @Parameter(description = "Number of items per page.", example = "20")
      @RequestParam(name = "size", required = false, defaultValue = CommonConstant.DEFAULT_PAGE_SIZE_STR) Integer size
  ) throws Exception {
    AdminListProductInput input = AdminListProductInput.builder()
        .id(id)
        .keyword(keyword)
        .minPrice(minPrice)
        .maxPrice(maxPrice)
        .status(status)
        .availableFrom(availableFrom)
        .isActive(isActive)
        .brandId(brandId)
        .categoryIds(categoryIds)
        .pagingRequest(PagingRequest.of(page - 1, size))
        .build();

    PagingResult<ProductEntity> productEntityPage = adminListProductUseCaseHandler.handle(input);

    PagingResult<ProductResponse> productResponsePage = ObjectMapperUtil.getInstance()
        .mapPage(productEntityPage, ProductResponse.class);
    return ApiResponseWrapper.successPage(productResponsePage);
  }

  @Operation(summary = "Retrieve details of a specific product by ID.")
  @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<ProductResponse> getProduct(
      @Parameter(description = "The unique identifier of the product.", example = "1")
      @PathVariable("productId") Integer productId) throws Exception {
    ProductEntity productEntity = adminGetProductUseCaseHandler.handle(productId);
    ProductResponse productResponse = ObjectMapperUtil.getInstance()
        .map(productEntity, ProductResponse.class);
    return ApiResponseWrapper.success(productResponse);
  }

  @Operation(summary = "Create a new product.")
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponseWrapper<Integer> createProduct(
      @Parameter(description = "Request object containing the details of the product to create.", required = true)
      @Valid @RequestBody AdminCreateProductRequest request) throws Exception {
    ProductEntity productEntity = AdminProductMapper.toProductEntity(request);
    Integer productId = adminCreateProductUseCaseHandler.handle(productEntity);
    return ApiResponseWrapper.success(productId);
  }

  @Operation(summary = "Update an existing product by ID.")
  @PutMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<Void> updateProduct(
      @Parameter(description = "The unique identifier of the product to update.", example = "1")
      @PathVariable("productId") Integer productId,
      @Parameter(description = "Request object containing the new details for the product.", required = true)
      @Valid @RequestBody AdminUpdateProductRequest request) throws Exception {
    ProductEntity productEntity = AdminProductMapper.toProductEntity(request);
    productEntity.setId(productId);
    adminUpdateProductUseCaseHandler.handle(productEntity);
    return ApiResponseWrapper.success();
  }

  @Operation(summary = "Delete a product by ID.")
  @DeleteMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<Void> deleteProduct(
      @Parameter(description = "The unique identifier of the product to delete.", example = "1")
      @PathVariable("productId") Integer productId) throws Exception {
    adminDeleteProductUseCaseHandler.handle(productId);
    return ApiResponseWrapper.success();
  }

  @Operation(summary = "Import products from a file.")
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<Void> importProduct(
      @Parameter(description = "The file containing products to import.")
      @RequestPart("file") MultipartFile file,
      @Parameter(description = "The type of the file (e.g., csv, xlsx).", example = "csv")
      @RequestPart("fileType") String fileType) throws Exception {
    byte[] fileContent = file.getBytes();
    AdminImportProductInput input = new AdminImportProductInput(FileType.of(fileType), fileContent);
    adminImportProductUseCaseHandler.handle(input);
    return ApiResponseWrapper.success();
  }

  @Operation(summary = "Export products based on optional filters.")
  @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<byte[]> export(
      @Parameter(description = "The unique identifier of the product to export.", example = "1")
      @RequestParam(name = "id", required = false) Integer id,
      @Parameter(description = "Keyword for searching products.", example = "T-Shirt")
      @RequestParam(name = "keyword", required = false) String keyword,
      @Parameter(description = "Minimum price for filtering products.", example = "10.00")
      @RequestParam(name = "min_price", required = false) BigDecimal minPrice,
      @Parameter(description = "Maximum price for filtering products.", example = "100.00")
      @RequestParam(name = "max_price", required = false) BigDecimal maxPrice,
      @Parameter(description = "Status of the product.", example = "IN_STOCK")
      @RequestParam(name = "status", required = false) ProductStatus status,
      @Parameter(description = "Date from which the product is available (ISO 8601 format).", example = "2023-01-01T00:00:00Z")
      @RequestParam(name = "available_from", required = false) Date availableFrom,
      @Parameter(description = "Indicates if the product is active.", example = "true")
      @RequestParam(name = "is_active", required = false) Boolean isActive,
      @Parameter(description = "Brand ID for filtering products.", example = "1")
      @RequestParam(name = "brand_id", required = false) Integer brandId,
      @Parameter(description = "List of category IDs for filtering products.", example = "[1, 2, 3]")
      @RequestParam(name = "category_ids", required = false) List<Integer> categoryIds,
      @Parameter(description = "The type of the file to export to (e.g., csv, xlsx).", example = "csv")
      @RequestParam(name = "file_type") FileType fileType
  ) throws Exception {
    AdminExportProductInput input = AdminExportProductInput.builder()
        .id(id)
        .keyword(keyword)
        .minPrice(minPrice)
        .maxPrice(maxPrice)
        .status(status)
        .availableFrom(availableFrom)
        .isActive(isActive)
        .brandId(brandId)
        .categoryIds(categoryIds)
        .fileType(fileType)
        .build();
    byte[] fileContent = adminExportProductUseCaseHandler.handle(input);

    // Exported file info
    String fileName = "export-product-" +
        DateUtil.now("yyyyMMddHHmmss") +
        "." +
        fileType.getExtension();

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(fileContent);
  }
}
