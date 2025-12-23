package org.atlas.infrastructure.api.server.rest.adapter.user.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.atlas.application.user.service.CartService;
import org.atlas.domain.user.entity.Cart;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.internalapi.user.model.CartResponse;
import org.atlas.infrastructure.api.server.rest.adapter.user.internal.mapper.InternalCartMapper;
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

  private final CartService cartService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve cart")
  public ApiResponseWrapper<CartResponse> retrieveCart(@RequestParam("userId") Integer userId) {
    Cart cart = cartService.retrieveCart(userId);
    CartResponse cartResponse = InternalCartMapper.INSTANCE.toCartResponse(cart);
    return ApiResponseWrapper.success(cartResponse);
  }
}
