package org.atlas.services.product.infrastructure.api.server.rest.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.product.infrastructure.api.server.rest.front.mapper.BrandMapper;
import org.atlas.services.product.infrastructure.api.server.rest.front.model.BrandResponse;
import org.atlas.services.product.port.in.front.service.BrandService;
import org.atlas.services.product.domain.entity.BrandEntity;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/front/brands")
@Validated
@RequiredArgsConstructor
public class BrandController {

  private final BrandService brandService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of all brands")
  public ApiResponseWrapper<List<BrandResponse>> retrieveAllBrand() {
    List<BrandEntity> brands = brandService.retrieveAllBrand();
    List<BrandResponse> responseData = ObjectMapperUtil.mapList(brands,
        BrandMapper.INSTANCE::toBrandResponse);
    return ApiResponseWrapper.success(responseData);
  }
}
