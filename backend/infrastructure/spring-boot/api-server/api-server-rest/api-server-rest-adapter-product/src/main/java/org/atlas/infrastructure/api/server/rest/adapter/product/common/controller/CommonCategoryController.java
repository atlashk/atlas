package org.atlas.infrastructure.api.server.rest.adapter.product.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.CategoryEntity;
import org.atlas.domain.product.usecase.common.handler.ListCategoryUseCaseHandler;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.adapter.product.shared.CategoryResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/common/products/categories")
@Validated
@RequiredArgsConstructor
public class CommonCategoryController {

  private final ListCategoryUseCaseHandler listCategoryUseCaseHandler;

  @Operation(summary = "List Categories", description = "Retrieves a list of all available categories.")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponseWrapper<List<CategoryResponse>> listCategory() throws Exception {
    List<CategoryEntity> categoryEntities = listCategoryUseCaseHandler.handle(null);
    List<CategoryResponse> categoryResponses = ObjectMapperUtil.getInstance()
        .mapList(categoryEntities, CategoryResponse.class);
    return ApiResponseWrapper.success(categoryResponses);
  }
}
