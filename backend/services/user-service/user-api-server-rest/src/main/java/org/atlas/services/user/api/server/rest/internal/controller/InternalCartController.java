package org.atlas.services.user.api.server.rest.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.internalapi.user.model.CartResponse;
import org.atlas.services.user.api.server.rest.internal.mapper.InternalCartMapper;
import org.atlas.services.user.application.service.CartService;
import org.atlas.services.user.domain.entity.Cart;
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
