package org.atlas.infrastructure.api.server.rest.impl.product.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.BrandEntity;
import org.atlas.domain.product.usecase.front.handler.ListBrandUseCaseHandler;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.impl.product.front.model.BrandResponse;
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

  private final ListBrandUseCaseHandler listBrandUseCaseHandler;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of all brands")
  public ApiResponseWrapper<List<BrandResponse>> listBrand() throws Exception {
    List<BrandEntity> brandEntities = listBrandUseCaseHandler.handle(null);
    List<BrandResponse> brandResponses = ObjectMapperUtil.getInstance()
        .mapList(brandEntities, BrandResponse.class);
    return ApiResponseWrapper.success(brandResponses);
  }
}
