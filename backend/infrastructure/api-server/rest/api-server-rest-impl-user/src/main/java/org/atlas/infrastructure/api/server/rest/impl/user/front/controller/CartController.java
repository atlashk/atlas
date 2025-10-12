package org.atlas.infrastructure.api.server.rest.impl.user.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.usecase.front.handler.AddCartItemUseCaseHandler;
import org.atlas.domain.user.usecase.front.handler.ClearCartUseCaseHandler;
import org.atlas.domain.user.usecase.front.handler.GetCartUseCaseHandler;
import org.atlas.domain.user.usecase.front.handler.RemoveCartItemUseCaseHandler;
import org.atlas.domain.user.usecase.front.handler.UpdateCartItemUseCaseHandler;
import org.atlas.domain.user.usecase.front.model.AddCartItemInput;
import org.atlas.domain.user.usecase.front.model.ClearCartInput;
import org.atlas.domain.user.usecase.front.model.GetCartInput;
import org.atlas.domain.user.usecase.front.model.RemoveCartItemInput;
import org.atlas.domain.user.usecase.front.model.UpdateCartItemInput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.impl.user.front.model.AddCartItemRequest;
import org.atlas.infrastructure.api.server.rest.impl.user.front.model.CartResponse;
import org.atlas.infrastructure.api.server.rest.impl.user.front.model.UpdateCartItemRequest;
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

  private final GetCartUseCaseHandler getCartUseCaseHandler;
  private final AddCartItemUseCaseHandler addCartItemUseCaseHandler;
  private final UpdateCartItemUseCaseHandler updateCartItemUseCaseHandler;
  private final RemoveCartItemUseCaseHandler removeCartItemUseCaseHandler;
  private final ClearCartUseCaseHandler clearCartUseCaseHandler;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get user cart", description = "Retrieve current user's cart with all items")
  public ApiResponseWrapper<CartResponse> getCart() throws Exception {
    GetCartInput input = GetCartInput.builder()
        .userId(Contexts.getUserId())
        .build();
    CartEntity cart = getCartUseCaseHandler.handle(input);
    CartResponse response = ObjectMapperUtil.getInstance()
        .map(cart, CartResponse.class);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/items/add", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Add item to cart", description = "Add a product to the user's cart")
  public ApiResponseWrapper<CartResponse> addCartItem(
      @Parameter(description = "Request object containing product and quantity", required = true)
      @Valid @RequestBody AddCartItemRequest request) throws Exception {
    AddCartItemInput input = AddCartItemInput.builder()
        .userId(Contexts.getUserId())
        .productId(request.getProductId())
        .quantity(request.getQuantity())
        .build();
    CartEntity cart = addCartItemUseCaseHandler.handle(input);
    CartResponse response = ObjectMapperUtil.getInstance()
        .map(cart, CartResponse.class);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/items/{productId}/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update cart item quantity", description = "Update the quantity of a specific item in cart")
  public ApiResponseWrapper<CartResponse> updateCartItem(
      @Parameter(description = "Product ID to remove", required = true)
      @PathVariable Integer productId,
      @Parameter(description = "Request object containing new quantity", required = true)
      @Valid @RequestBody UpdateCartItemRequest request) throws Exception {
    UpdateCartItemInput input = UpdateCartItemInput.builder()
        .userId(Contexts.getUserId())
        .productId(productId)
        .quantity(request.getQuantity())
        .build();
    CartEntity cart = updateCartItemUseCaseHandler.handle(input);
    CartResponse response = ObjectMapperUtil.getInstance()
        .map(cart, CartResponse.class);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/items/{productId}/remove", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Remove item from cart", description = "Remove a specific product from the user's cart")
  public ApiResponseWrapper<CartResponse> removeCartItem(
      @Parameter(description = "Product ID to remove", required = true)
      @PathVariable Integer productId) throws Exception {
    RemoveCartItemInput input = RemoveCartItemInput.builder()
        .userId(Contexts.getUserId())
        .productId(productId)
        .build();
    CartEntity cart = removeCartItemUseCaseHandler.handle(input);
    CartResponse response = ObjectMapperUtil.getInstance()
        .map(cart, CartResponse.class);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/clear", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Clear cart", description = "Remove all items from the user's cart")
  public ApiResponseWrapper<CartResponse> clearCart() throws Exception {
    ClearCartInput input = ClearCartInput.builder()
        .userId(Contexts.getUserId())
        .build();
    CartEntity cart = clearCartUseCaseHandler.handle(input);
    CartResponse response = ObjectMapperUtil.getInstance()
        .map(cart, CartResponse.class);
    return ApiResponseWrapper.success(response);
  }
}
