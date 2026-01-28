package org.atlas.product.api.server.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.product.application.service.BrandService;
import org.atlas.product.domain.entity.Brand;
import org.atlas.common.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.common.framework.util.ObjectMapperUtil;
import org.atlas.product.api.server.rest.mapper.BrandMapper;
import org.atlas.product.api.server.rest.model.BrandResponse;
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
