package org.atlas.infrastructure.api.server.rest.adapter.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.application.user.service.CartService;
import org.atlas.domain.user.entity.Cart;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.context.Contexts;
import org.atlas.infrastructure.api.server.rest.adapter.user.mapper.CartMapper;
import org.atlas.infrastructure.api.server.rest.adapter.user.model.AddCartItemRequest;
import org.atlas.infrastructure.api.server.rest.adapter.user.model.CartResponse;
import org.atlas.infrastructure.api.server.rest.adapter.user.model.UpdateCartItemRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
@Validated
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve user cart", description = "Retrieve current user's cart with all items")
  public ApiResponseWrapper<CartResponse> retrieveCart() {
    Cart cart = cartService.retrieveCart(Contexts.getUserId());
    CartResponse response = CartMapper.INSTANCE.toCartResponse(cart);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/items/add", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Add item to cart", description = "Add a product to the user's cart")
  public ApiResponseWrapper<CartResponse> addCartItem(
      @Parameter(description = "Request object containing product and quantity", required = true)
      @Valid @RequestBody AddCartItemRequest request) {
    Cart cart = cartService.addCartItem(Contexts.getUserId(), request.getProductId(),
        request.getQuantity());
    CartResponse response = CartMapper.INSTANCE.toCartResponse(cart);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/items/{productId}/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update cart item quantity", description = "Update the quantity of a specific item in cart")
  public ApiResponseWrapper<CartResponse> updateCartItem(
      @Parameter(description = "Product ID to remove", required = true)
      @PathVariable Integer productId,
      @Parameter(description = "Request object containing new quantity", required = true)
      @Valid @RequestBody UpdateCartItemRequest request) {
    Cart cart = cartService.updateQuantity(Contexts.getUserId(), productId, request.getQuantity());
    CartResponse response = CartMapper.INSTANCE.toCartResponse(cart);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/items/{productId}/remove", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Remove item from cart", description = "Remove a specific product from the user's cart")
  public ApiResponseWrapper<CartResponse> removeCartItem(
      @Parameter(description = "Product ID to remove", required = true)
      @PathVariable Integer productId) {
    Cart cart = cartService.removeCartItem(Contexts.getUserId(), productId);
    CartResponse response = CartMapper.INSTANCE.toCartResponse(cart);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/clear", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Clear cart", description = "Remove all items from the user's cart")
  public ApiResponseWrapper<CartResponse> clearCart() {
    Cart cart = cartService.clearCart(Contexts.getUserId());
    CartResponse response = CartMapper.INSTANCE.toCartResponse(cart);
    return ApiResponseWrapper.success(response);
  }
}
