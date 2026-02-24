package org.atlas.services.catalog.infrastructure.api.server.rest.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.file.FileType;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.DateUtil;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.mapper.ProductAdminMapper;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.model.admin.CreateProductRequest;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.model.admin.ExportProductRequest;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.model.admin.ProductResponse;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.model.admin.RetrieveProductListRequest;
import org.atlas.services.catalog.infrastructure.api.server.rest.product.model.admin.UpdateProductRequest;
import org.atlas.services.catalog.port.in.product.model.admin.CreateProductInput;
import org.atlas.services.catalog.port.in.product.model.admin.ExportProductInput;
import org.atlas.services.catalog.port.in.product.model.admin.ImportProductInput;
import org.atlas.services.catalog.port.in.product.model.admin.RetrieveProductListInput;
import org.atlas.services.catalog.port.in.product.model.admin.UpdateProductInput;
import org.atlas.services.catalog.port.in.product.service.ProductAdminService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/products")
@Validated
@RequiredArgsConstructor
public class ProductAdminController {

  private final ProductAdminService productAdminService;

  @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of products with optional filtering and pagination")
  public ApiResponseWrapper<List<ProductResponse>> retrieveProductList(
      @Parameter(description = "Request object containing filters and pagination", required = true)
      @Valid @RequestBody RetrieveProductListRequest request
  ) {
    RetrieveProductListInput input = ProductAdminMapper.INSTANCE.toRetrieveProductListInput(request);
    input.setPagingRequest(PagingRequest.of(request.getPage() - 1, request.getSize()));

    PagingResult<ProductEntity> productPage = productAdminService.retrieveProductList(input);
    PagingResult<ProductResponse> responseData = MapperUtil.mapPage(productPage,
        ProductAdminMapper.INSTANCE::toProductResponse);
    return ApiResponseWrapper.successPage(responseData);
  }

  @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve details of a specific product by ID")
  public ApiResponseWrapper<ProductResponse> retrieveProduct(
      @Parameter(name = "productId", description = "The unique identifier of the product", example = "1")
      @PathVariable String productId) {
    ProductEntity product = productAdminService.retrieveProduct(productId);

    ProductResponse response = ProductAdminMapper.INSTANCE.toProductResponse(product);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new product")
  public ApiResponseWrapper<String> createProduct(
      @Parameter(description = "Request object containing product details", required = true)
      @Valid @RequestPart("request") CreateProductRequest request,
      @Parameter(description = "Product image file")
      @RequestPart(value = "image") MultipartFile imageFile) throws Exception {
    ProductEntity product = ProductAdminMapper.INSTANCE.toProduct(request);
    CreateProductInput input = CreateProductInput.builder()
        .product(product)
        .imageBytes(imageFile.getBytes())
        .imageContentType(imageFile.getContentType())
        .build();

    String responseData = productAdminService.createProduct(input);
    return ApiResponseWrapper.success(responseData);
  }

  @PutMapping(value = "/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update an existing product by ID")
  public ApiResponseWrapper<Void> updateProduct(
      @Parameter(name = "productId", description = "The unique identifier of the product to update", example = "1")
      @PathVariable String productId,
      @Parameter(description = "Request object containing the new details for the product", required = true)
      @Valid @RequestPart("request") UpdateProductRequest request,
      @Parameter(description = "Product image file")
      @RequestPart(value = "image", required = false) MultipartFile imageFile) throws Exception {
    ProductEntity product = ProductAdminMapper.INSTANCE.toProduct(request);
    product.setId(productId);
    UpdateProductInput input = UpdateProductInput.builder()
        .product(product)
        .build();
    if (imageFile != null) {
      input.setImageBytes(imageFile.getBytes());
      input.setImageContentType(imageFile.getContentType());
    }

    productAdminService.updateProduct(input);
    return ApiResponseWrapper.success();
  }

  @DeleteMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete a product by ID")
  public ApiResponseWrapper<Void> deleteProduct(
      @Parameter(name = "productId", description = "The unique identifier of the product to delete", example = "1")
      @PathVariable String productId) {
    productAdminService.deleteProduct(productId);
    return ApiResponseWrapper.success();
  }

  @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Import products from a file")
  public ApiResponseWrapper<Void> importProduct(
      @Parameter(name = "file", description = "The file containing products to import")
      @RequestPart("file") MultipartFile file,
      @Parameter(name = "file_type", description = "The type of the file (e.g., csv, xlsx)", example = "csv")
      @RequestPart("file_type") FileType fileType) throws Exception {
    byte[] fileContent = file.getBytes();
    ImportProductInput input = new ImportProductInput(fileType, fileContent);

    productAdminService.importProduct(input);
    return ApiResponseWrapper.success();
  }

  @PostMapping(value = "/export", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @Operation(summary = "Export products based on optional filters")
  public ResponseEntity<byte[]> export(
      @Parameter(description = "Request object containing filters and export settings", required = true)
      @Valid @RequestBody ExportProductRequest request
  ) throws Exception {
    ExportProductInput input = ProductAdminMapper.INSTANCE.toExportProductInput(request);
    byte[] fileContent = productAdminService.exportProduct(input);

    // Exported file info
    String fileName = String.format("export-product-%s.%s",
        DateUtil.now("yyyyMMddHHmmss"),
        request.getFileType().getExtension());
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(fileContent);
  }

  @GetMapping("/statistics/total-count")
  @Operation(summary = "Retrieve the total product count")
  public ApiResponseWrapper<Long> retrieveTotalProductCount() {
    Long responseData = productAdminService.retrieveTotalProductCount();
    return ApiResponseWrapper.success(responseData);
  }
}
