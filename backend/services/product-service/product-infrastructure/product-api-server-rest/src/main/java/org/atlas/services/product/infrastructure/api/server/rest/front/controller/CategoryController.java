package org.atlas.services.product.infrastructure.api.server.rest.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.product.infrastructure.api.server.rest.front.mapper.CategoryMapper;
import org.atlas.services.product.infrastructure.api.server.rest.front.model.CategoryResponse;
import org.atlas.services.product.port.in.front.service.CategoryService;
import org.atlas.services.product.domain.entity.CategoryEntity;
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
    List<CategoryResponse> responseData = ObjectMapperUtil.mapList(categories,
        CategoryMapper.INSTANCE::toCategoryResponse);
    return ApiResponseWrapper.success(responseData);
  }
}
