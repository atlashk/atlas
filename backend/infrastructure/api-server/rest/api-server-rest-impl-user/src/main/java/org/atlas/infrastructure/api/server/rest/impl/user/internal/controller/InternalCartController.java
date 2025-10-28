package org.atlas.infrastructure.api.server.rest.impl.user.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.Cart;
import org.atlas.domain.user.usecase.internal.handler.InternalGetCartUseCaseHandler;
import org.atlas.domain.user.usecase.internal.model.InternalGetCartInput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.user.model.CartResponse;
import org.atlas.infrastructure.api.server.rest.impl.user.internal.mapper.InternalCartMapper;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/carts")
@Validated
@RequiredArgsConstructor
public class InternalCartController {

  private final InternalGetCartUseCaseHandler internalGetCartUseCaseHandler;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get cart by user ID")
  public ApiResponseWrapper<CartResponse> getCart(@RequestParam("userId") Integer userId)
      throws Exception {
    InternalGetCartInput input = new InternalGetCartInput(userId);
    Cart cart = internalGetCartUseCaseHandler.handle(input);
    CartResponse cartResponse = InternalCartMapper.INSTANCE.toCartResponse(cart);
    return ApiResponseWrapper.success(cartResponse);
  }
}
