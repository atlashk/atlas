package org.atlas.services.catalog.api.rest.category.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.api.rest.category.mapper.CategoryMapper;
import org.atlas.services.catalog.api.rest.category.model.CategoryResponse;
import org.atlas.services.catalog.domain.entity.Category;
import org.atlas.services.catalog.port.in.category.service.CategoryService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/categories")
@Validated
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of all categories")
  public ApiResponseWrapper<List<CategoryResponse>> retrieveAllCategory() {
    List<Category> categories = categoryService.retrieveAllCategory();
    List<CategoryResponse> responseData = MapperUtil.mapList(categories,
        CategoryMapper.INSTANCE::toCategoryResponse);
    return ApiResponseWrapper.success(responseData);
  }
}
