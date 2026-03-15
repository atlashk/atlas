package org.atlas.services.order.api.rest.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.services.order.api.rest.cart.mapper.CartMapper;
import org.atlas.services.order.api.rest.cart.model.AddCartItemRequest;
import org.atlas.services.order.api.rest.cart.model.CartResponse;
import org.atlas.services.order.api.rest.cart.model.UpdateCartItemRequest;
import org.atlas.services.order.domain.entity.CartItemEntity;
import org.atlas.services.order.port.in.cart.service.CartService;
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
    java.util.List<CartItemEntity> cartItems = cartService.retrieveCart();
    CartResponse response = CartMapper.INSTANCE.toCartResponse(cartItems);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/items/add", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Add item to cart", description = "Add a product to the user's cart")
  public ApiResponseWrapper<CartResponse> addCartItem(
      @Parameter(description = "Request object containing product and quantity", required = true)
      @Valid @RequestBody AddCartItemRequest request) {
    java.util.List<CartItemEntity> cartItems = cartService.addCartItem(request.getProductId(),
        request.getQuantity());
    CartResponse response = CartMapper.INSTANCE.toCartResponse(cartItems);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/items/{productId}/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update cart item quantity", description = "Update the quantity of a specific item in cart")
  public ApiResponseWrapper<CartResponse> updateCartItem(
      @Parameter(description = "Product ID to remove", required = true)
      @PathVariable String productId,
      @Parameter(description = "Request object containing new quantity", required = true)
      @Valid @RequestBody UpdateCartItemRequest request) {
    java.util.List<CartItemEntity> cartItems = cartService.updateQuantity(productId,
        request.getQuantity());
    CartResponse response = CartMapper.INSTANCE.toCartResponse(cartItems);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/items/{productId}/remove", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Remove item from cart", description = "Remove a specific product from the user's cart")
  public ApiResponseWrapper<CartResponse> removeCartItem(
      @Parameter(description = "Product ID to remove", required = true)
      @PathVariable String productId) {
    java.util.List<CartItemEntity> cartItems = cartService.removeCartItem(productId);
    CartResponse response = CartMapper.INSTANCE.toCartResponse(cartItems);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/clear", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Clear cart", description = "Remove all items from the user's cart")
  public ApiResponseWrapper<CartResponse> clearCart() {
    java.util.List<CartItemEntity> cartItems = cartService.clearCart();
    CartResponse response = CartMapper.INSTANCE.toCartResponse(cartItems);
    return ApiResponseWrapper.success(response);
  }
}
