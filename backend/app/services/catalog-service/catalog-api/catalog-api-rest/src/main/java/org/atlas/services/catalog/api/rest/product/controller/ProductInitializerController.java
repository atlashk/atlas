package org.atlas.services.catalog.api.rest.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.services.catalog.port.in.product.service.ProductInitializerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/admin/initialize")
@RequiredArgsConstructor
public class ProductInitializerController {

  private final ProductInitializerService productInitializerService;

  @PostMapping(value = "/image-bucket", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  @Operation(summary = "Trigger async initialization of the product image bucket")
  public ApiResponseWrapper<Void> initializeImageBucket() throws Exception {
    productInitializerService.initializeImageBucket();
    return ApiResponseWrapper.success();
  }

  @PostMapping(value = "/search-data", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  @Operation(summary = "Trigger async initialization of product search index data")
  public ApiResponseWrapper<Void> initializeSearchData() throws Exception {
    productInitializerService.initializeSearchData();
    return ApiResponseWrapper.success();
  }

  @PostMapping(value = "/vector-store", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  @Operation(summary = "Trigger async initialization of the product vector store")
  public ApiResponseWrapper<Void> initializeVectorStore() throws Exception {
    productInitializerService.initializeVectorStore();
    return ApiResponseWrapper.success();
  }
}
