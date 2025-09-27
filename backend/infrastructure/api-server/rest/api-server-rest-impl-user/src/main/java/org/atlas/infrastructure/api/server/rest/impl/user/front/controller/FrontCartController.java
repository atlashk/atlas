package org.atlas.infrastructure.api.server.rest.impl.user.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.usecase.front.handler.FrontAddCartItemUseCaseHandler;
import org.atlas.domain.user.usecase.front.handler.FrontClearCartUseCaseHandler;
import org.atlas.domain.user.usecase.front.handler.FrontGetCartUseCaseHandler;
import org.atlas.domain.user.usecase.front.handler.FrontRemoveCartItemUseCaseHandler;
import org.atlas.domain.user.usecase.front.handler.FrontUpdateCartItemUseCaseHandler;
import org.atlas.domain.user.usecase.front.model.FrontAddCartItemInput;
import org.atlas.domain.user.usecase.front.model.FrontClearCartInput;
import org.atlas.domain.user.usecase.front.model.FrontGetCartInput;
import org.atlas.domain.user.usecase.front.model.FrontRemoveCartItemInput;
import org.atlas.domain.user.usecase.front.model.FrontUpdateCartItemInput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.impl.user.front.model.CartResponse;
import org.atlas.infrastructure.api.server.rest.impl.user.front.model.FrontAddCartItemRequest;
import org.atlas.infrastructure.api.server.rest.impl.user.front.model.FrontUpdateCartItemRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/front/cart")
@Validated
@RequiredArgsConstructor
@Tag(name = "Frontend Cart", description = "Cart operations for frontend users")
public class FrontCartController {

  private final FrontGetCartUseCaseHandler frontGetCartUseCaseHandler;
  private final FrontAddCartItemUseCaseHandler frontAddCartItemUseCaseHandler;
  private final FrontUpdateCartItemUseCaseHandler frontUpdateCartItemUseCaseHandler;
  private final FrontRemoveCartItemUseCaseHandler frontRemoveCartItemUseCaseHandler;
  private final FrontClearCartUseCaseHandler frontClearCartUseCaseHandler;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get user cart", description = "Retrieve current user's cart with all items")
  public ApiResponseWrapper<CartResponse> getCart() throws Exception {
    FrontGetCartInput input = FrontGetCartInput.builder()
        .userId(Contexts.getUserId())
        .build();
    CartEntity cart = frontGetCartUseCaseHandler.handle(input);
    CartResponse response = ObjectMapperUtil.getInstance()
        .map(cart, CartResponse.class);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/items/add", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Add item to cart", description = "Add a product to the user's cart")
  public ApiResponseWrapper<CartResponse> addCartItem(
      @Parameter(description = "Request object containing product and quantity", required = true)
      @Valid @RequestBody FrontAddCartItemRequest request) throws Exception {
    FrontAddCartItemInput input = FrontAddCartItemInput.builder()
        .userId(Contexts.getUserId())
        .productId(request.getProductId())
        .quantity(request.getQuantity())
        .build();
    CartEntity cart = frontAddCartItemUseCaseHandler.handle(input);
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
      @Valid @RequestBody FrontUpdateCartItemRequest request) throws Exception {
    FrontUpdateCartItemInput input = FrontUpdateCartItemInput.builder()
        .userId(Contexts.getUserId())
        .productId(productId)
        .quantity(request.getQuantity())
        .build();
    CartEntity cart = frontUpdateCartItemUseCaseHandler.handle(input);
    CartResponse response = ObjectMapperUtil.getInstance()
        .map(cart, CartResponse.class);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/items/{productId}/remove", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Remove item from cart", description = "Remove a specific product from the user's cart")
  public ApiResponseWrapper<CartResponse> removeCartItem(
      @Parameter(description = "Product ID to remove", required = true)
      @PathVariable Integer productId) throws Exception {
    FrontRemoveCartItemInput input = FrontRemoveCartItemInput.builder()
        .userId(Contexts.getUserId())
        .productId(productId)
        .build();
    CartEntity cart = frontRemoveCartItemUseCaseHandler.handle(input);
    CartResponse response = ObjectMapperUtil.getInstance()
        .map(cart, CartResponse.class);
    return ApiResponseWrapper.success(response);
  }

  @PostMapping(value = "/clear", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Clear cart", description = "Remove all items from the user's cart")
  public ApiResponseWrapper<CartResponse> clearCart() throws Exception {
    FrontClearCartInput input = FrontClearCartInput.builder()
        .userId(Contexts.getUserId())
        .build();
    CartEntity cart = frontClearCartUseCaseHandler.handle(input);
    CartResponse response = ObjectMapperUtil.getInstance()
        .map(cart, CartResponse.class);
    return ApiResponseWrapper.success(response);
  }
}
