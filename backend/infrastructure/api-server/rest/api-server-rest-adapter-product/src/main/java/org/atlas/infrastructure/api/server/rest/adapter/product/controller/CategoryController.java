package org.atlas.infrastructure.api.server.rest.adapter.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.application.product.service.CategoryService;
import org.atlas.domain.product.entity.Category;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.adapter.product.mapper.CategoryMapper;
import org.atlas.infrastructure.api.server.rest.adapter.product.model.CategoryResponse;
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

  private final CategoryService categoryService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of all categories")
  public ApiResponseWrapper<List<CategoryResponse>> retrieveAllCategory() {
    List<Category> categories = categoryService.retrieveAllCategory();
    List<CategoryResponse> responseData = ObjectMapperUtil.mapList(categories,
        CategoryMapper.INSTANCE::toCategoryResponse);
    return ApiResponseWrapper.success(responseData);
  }
}
