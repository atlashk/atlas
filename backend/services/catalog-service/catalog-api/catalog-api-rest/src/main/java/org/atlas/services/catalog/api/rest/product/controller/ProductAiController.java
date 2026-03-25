package org.atlas.services.catalog.api.rest.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.api.rest.product.mapper.ProductMapper;
import org.atlas.services.catalog.api.rest.product.model.ProductResponse;
import org.atlas.services.catalog.api.rest.product.model.RetrieveProductListRequest;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.port.in.product.model.RetrieveProductListInput;
import org.atlas.services.catalog.port.in.product.service.ProductService;
import org.atlas.services.catalog.port.out.ai.rag.model.ChatInput;
import org.atlas.services.catalog.port.out.ai.rag.service.ProductRagService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/ai")
@Validated
@RequiredArgsConstructor
public class ProductAiController {

  private final ProductRagService productRagService;

  @PostMapping(value = "/rag/chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Chat with product RAG service")
  public ApiResponseWrapper<String> ragChat(
      @Parameter(description = "Request object containing chat input", required = true)
      @RequestBody ChatInput input
  ) {
    String responseData = productRagService.chat(input);
    return ApiResponseWrapper.success(responseData);
  }
}
