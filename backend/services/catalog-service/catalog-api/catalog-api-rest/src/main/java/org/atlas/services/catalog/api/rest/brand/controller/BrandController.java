package org.atlas.services.catalog.api.rest.brand.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.domain.entity.BrandEntity;
import org.atlas.services.catalog.api.rest.brand.mapper.BrandMapper;
import org.atlas.services.catalog.api.rest.brand.model.BrandResponse;
import org.atlas.services.catalog.port.in.brand.service.BrandService;
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
    List<BrandEntity> brands = brandService.retrieveAllBrand();
    List<BrandResponse> responseData = MapperUtil.mapList(brands,
        BrandMapper.INSTANCE::toBrandResponse);
    return ApiResponseWrapper.success(responseData);
  }
}
