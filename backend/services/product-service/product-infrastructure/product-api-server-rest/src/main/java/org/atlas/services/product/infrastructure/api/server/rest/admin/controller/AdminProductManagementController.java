package org.atlas.services.product.infrastructure.api.server.rest.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.libs.framework.domain.product.ProductStatus;
import org.atlas.libs.framework.file.FileType;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.DateUtil;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.product.infrastructure.api.server.rest.admin.mapper.AdminProductMapper;
import org.atlas.services.product.infrastructure.api.server.rest.admin.model.AdminCreateProductRequest;
import org.atlas.services.product.infrastructure.api.server.rest.admin.model.AdminProductResponse;
import org.atlas.services.product.infrastructure.api.server.rest.admin.model.AdminUpdateProductRequest;
import org.atlas.services.product.port.in.admin.model.AdminCreateProductInput;
import org.atlas.services.product.port.in.admin.model.AdminExportProductInput;
import org.atlas.services.product.port.in.admin.model.AdminImportProductInput;
import org.atlas.services.product.port.in.admin.model.AdminRetrieveProductListInput;
import org.atlas.services.product.port.in.admin.model.AdminUpdateProductInput;
import org.atlas.services.product.port.in.admin.service.AdminProductService;
import org.atlas.services.product.domain.entity.Product;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/products")
@Validated
@RequiredArgsConstructor
public class AdminProductManagementController {

  private final AdminProductService adminProductService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of products with optional filtering and pagination")
  public ApiResponseWrapper<List<AdminProductResponse>> retrieveProductList(
      @Parameter(name = "id", description = "The unique identifier of the product", example = "1")
      @RequestParam(name = "id", required = false) Integer id,
      @Parameter(name = "keyword", description = "Keyword for searching products", example = "T-Shirt")
      @RequestParam(name = "keyword", required = false) String keyword,
      @Parameter(name = "minPrice", description = "Minimum price for filtering products", example = "10.00")
      @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
      @Parameter(name = "maxPrice", description = "Maximum price for filtering products", example = "100.00")
      @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
      @Parameter(name = "status", description = "Status of the product", example = "IN_STOCK")
      @RequestParam(name = "status", required = false) ProductStatus status,
      @Parameter(name = "availableFrom", description = "Date from which the product is available (ISO 8601 format)", example = "2023-01-01T00:00:00Z")
      @RequestParam(name = "availableFrom", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date availableFrom,
      @Parameter(name = "isActive", description = "Indicates if the product is active", example = "true")
      @RequestParam(name = "isActive", required = false) Boolean isActive,
      @Parameter(name = "brandId", description = "Brand ID for filtering products", example = "1")
      @RequestParam(name = "brandId", required = false) Integer brandId,
      @Parameter(name = "categoryIds", description = "List of category IDs for filtering products", example = "[1, 2, 3]")
      @RequestParam(name = "categoryIds", required = false) List<Integer> categoryIds,
      @Parameter(name = "page", description = "Page number for pagination", example = "1")
      @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
      @Parameter(name = "size", description = "Number of items per page", example = "20")
      @RequestParam(name = "size", required = false, defaultValue = CommonConstant.DEFAULT_PAGE_SIZE_STR) Integer size
  ) {
    AdminRetrieveProductListInput input = AdminRetrieveProductListInput.builder()
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
    PagingResult<Product> productPage = adminProductService.retrieveProductList(input);
    PagingResult<AdminProductResponse> responseData = ObjectMapperUtil.mapPage(productPage,
        AdminProductMapper.INSTANCE::toProductResponse);
    return ApiResponseWrapper.successPage(responseData);
  }

  @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve details of a specific product by ID")
  public ApiResponseWrapper<AdminProductResponse> retrieveProduct(
      @Parameter(name = "productId", description = "The unique identifier of the product", example = "1")
      @PathVariable Integer productId) {
    Product product = adminProductService.retrieveProduct(productId);
    AdminProductResponse response = AdminProductMapper.INSTANCE.toProductResponse(product);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new product")
  public ApiResponseWrapper<Integer> createProduct(
      @Parameter(description = "Request object containing product details", required = true)
      @Valid @RequestPart("request") AdminCreateProductRequest request,
      @Parameter(description = "Product image file")
      @RequestPart(value = "image") MultipartFile imageFile) throws Exception {
    Product product = AdminProductMapper.INSTANCE.toProduct(request);
    AdminCreateProductInput input = AdminCreateProductInput.builder()
        .product(product)
        .imageBytes(imageFile.getBytes())
        .imageContentType(imageFile.getContentType())
        .build();
    Integer responseData = adminProductService.createProduct(input);
    return ApiResponseWrapper.success(responseData);
  }

  @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update an existing product by ID")
  public ApiResponseWrapper<Void> updateProduct(
      @Parameter(name = "productId", description = "The unique identifier of the product to update", example = "1")
      @PathVariable Integer productId,
      @Parameter(description = "Request object containing the new details for the product", required = true)
      @Valid @RequestPart("request") AdminUpdateProductRequest request,
      @Parameter(description = "Product image file")
      @RequestPart(value = "image", required = false) MultipartFile imageFile) throws Exception {
    Product product = AdminProductMapper.INSTANCE.toProduct(request);
    product.setId(productId);
    AdminUpdateProductInput input = AdminUpdateProductInput.builder()
        .product(product)
        .build();
    if (imageFile != null) {
      input.setImageBytes(imageFile.getBytes());
      input.setImageContentType(imageFile.getContentType());
    }
    adminProductService.updateProduct(input);
    return ApiResponseWrapper.success();
  }

  @DeleteMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete a product by ID")
  public ApiResponseWrapper<Void> deleteProduct(
      @Parameter(name = "productId", description = "The unique identifier of the product to delete", example = "1")
      @PathVariable Integer productId) {
    adminProductService.deleteProduct(productId);
    return ApiResponseWrapper.success();
  }

  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Import products from a file")
  public ApiResponseWrapper<Void> importProduct(
      @Parameter(name = "file", description = "The file containing products to import")
      @RequestPart("file") MultipartFile file,
      @Parameter(name = "file_type", description = "The type of the file (e.g., csv, xlsx)", example = "csv")
      @RequestPart("file_type") FileType fileType) throws Exception {
    byte[] fileContent = file.getBytes();
    AdminImportProductInput input = new AdminImportProductInput(fileType, fileContent);
    adminProductService.importProduct(input);
    return ApiResponseWrapper.success();
  }

  @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Export products based on optional filters")
  public ResponseEntity<byte[]> export(
      @Parameter(name = "id", description = "The unique identifier of the product to export", example = "1")
      @RequestParam(name = "id", required = false) Integer id,
      @Parameter(name = "keyword", description = "Keyword for searching products", example = "T-Shirt")
      @RequestParam(name = "keyword", required = false) String keyword,
      @Parameter(name = "minPrice", description = "Minimum price for filtering products", example = "10.00")
      @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
      @Parameter(name = "maxPrice", description = "Maximum price for filtering products", example = "100.00")
      @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
      @Parameter(name = "status", description = "Status of the product", example = "IN_STOCK")
      @RequestParam(name = "status", required = false) ProductStatus status,
      @Parameter(name = "availableFrom", description = "Date from which the product is available (ISO 8601 format)", example = "2023-01-01T00:00:00Z")
      @RequestParam(name = "availableFrom", required = false) Date availableFrom,
      @Parameter(name = "isActive", description = "Indicates if the product is active", example = "true")
      @RequestParam(name = "isActive", required = false) Boolean isActive,
      @Parameter(name = "brandId", description = "Brand ID for filtering products", example = "1")
      @RequestParam(name = "brandId", required = false) Integer brandId,
      @Parameter(name = "categoryIds", description = "List of category IDs for filtering products", example = "[1, 2, 3]")
      @RequestParam(name = "categoryIds", required = false) List<Integer> categoryIds,
      @Parameter(name = "file_type", description = "The type of the file to export to (e.g., csv, xlsx)", example = "csv")
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
    byte[] fileContent = adminProductService.exportProduct(input);

    // Exported file info
    String fileName = String.format("export-product-%s.%s",
        DateUtil.now("yyyyMMddHHmmss"),
        fileType.getExtension());

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(fileContent);
  }
}
