package org.atlas.services.catalog.infrastructure.api.server.rest.category.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.domain.entity.CategoryEntity;
import org.atlas.services.catalog.infrastructure.api.server.rest.category.mapper.CategoryMapper;
import org.atlas.services.catalog.infrastructure.api.server.rest.category.model.CategoryResponse;
import org.atlas.services.catalog.port.in.category.service.CategoryService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/front/categories")
@Validated
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of all categories")
  public ApiResponseWrapper<List<CategoryResponse>> retrieveAllCategory() {
    List<CategoryEntity> categories = categoryService.retrieveAllCategory();
    List<CategoryResponse> responseData = MapperUtil.mapList(categories,
        CategoryMapper.INSTANCE::toCategoryResponse);
    return ApiResponseWrapper.success(responseData);
  }
}
