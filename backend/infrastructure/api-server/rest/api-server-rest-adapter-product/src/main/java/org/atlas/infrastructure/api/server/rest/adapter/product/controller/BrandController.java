package org.atlas.infrastructure.api.server.rest.adapter.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.application.product.service.BrandService;
import org.atlas.domain.product.entity.Brand;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.adapter.product.mapper.BrandMapper;
import org.atlas.infrastructure.api.server.rest.adapter.product.model.BrandResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/brands")
@Validated
@RequiredArgsConstructor
public class BrandController {

  private final BrandService brandService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of all brands")
  public ApiResponseWrapper<List<BrandResponse>> retrieveAllBrand() {
    List<Brand> brands = brandService.retrieveAllBrand();
    List<BrandResponse> responseData = ObjectMapperUtil.mapList(brands,
        BrandMapper.INSTANCE::toBrandResponse);
    return ApiResponseWrapper.success(responseData);
  }
}
