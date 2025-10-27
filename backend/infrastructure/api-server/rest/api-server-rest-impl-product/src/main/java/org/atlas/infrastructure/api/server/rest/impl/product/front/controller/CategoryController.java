package org.atlas.infrastructure.api.server.rest.impl.product.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.CategoryEntity;
import org.atlas.domain.product.usecase.front.handler.ListCategoryUseCaseHandler;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.impl.product.front.model.CategoryResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@Validated
@RequiredArgsConstructor
public class CategoryController {

  private final ListCategoryUseCaseHandler listCategoryUseCaseHandler;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of all categories")
  public ApiResponseWrapper<List<CategoryResponse>> listCategory() throws Exception {
    List<CategoryEntity> categoryEntities = listCategoryUseCaseHandler.handle(null);
    List<CategoryResponse> categoryResponses = ObjectMapperUtil.getInstance()
        .mapList(categoryEntities, CategoryResponse.class);
    return ApiResponseWrapper.success(categoryResponses);
  }
}
