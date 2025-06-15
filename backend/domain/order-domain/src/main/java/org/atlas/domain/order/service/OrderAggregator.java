package org.atlas.domain.order.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.entity.ProductEntity;
import org.atlas.domain.order.entity.UserEntity;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.service.DomainService;
import org.atlas.framework.error.AppError;
import org.atlas.framework.internalapi.product.ProductApiPort;
import org.atlas.framework.internalapi.product.model.ListProductRequest;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.atlas.framework.internalapi.user.UserApiPort;
import org.atlas.framework.internalapi.user.model.ListUserRequest;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.framework.objectmapper.ObjectMapperUtil;

import lombok.RequiredArgsConstructor;

@DomainService
@RequiredArgsConstructor
public class OrderAggregator {

  private final UserApiPort userApiPort;
  private final ProductApiPort productApiPort;

  public void aggregate(List<OrderEntity> orderEntities, boolean ignoreNotFound) {
    if (CollectionUtils.isEmpty(orderEntities)) {
      return;
    }
    loadUsers(orderEntities, ignoreNotFound);
    loadProducts(orderEntities, ignoreNotFound);
  }

  private void loadUsers(List<OrderEntity> orderEntities, boolean ignoreNotFound) {
    // Collect user IDs
    List<Integer> userIds = orderEntities.stream()
        .map(orderEntity -> orderEntity.getUser().getId())
        .distinct()
        .toList();
    if (CollectionUtils.isEmpty(userIds)) {
      return;
    }

    // Call user-service to fetch user info
    ListUserRequest request = new ListUserRequest(userIds);
    List<UserResponse> userResponses = userApiPort.call(request);
    if (CollectionUtils.isEmpty(userResponses)) {
      if (ignoreNotFound) {
        return;
      } else {
        throw new DomainException(AppError.USER_NOT_FOUND);
      }
    }

    // Update order's user
    Map<Integer, UserResponse> userResponseMap = userResponses.stream()
        .collect(Collectors.toMap(UserResponse::getId, Function.identity()));
    orderEntities.forEach(orderEntity -> {
      UserResponse userResponse = userResponseMap.get(orderEntity.getUser().getId());
      if (userResponse != null) {
        UserEntity userEntity = ObjectMapperUtil.getInstance()
            .map(userResponse, UserEntity.class);
        orderEntity.setUser(userEntity);
      } else {
        if (ignoreNotFound) {
          throw new DomainException(AppError.USER_NOT_FOUND,
              String.format("User %d not found", orderEntity.getUser().getId()));
        }
      }
    });
  }

  private void loadProducts(List<OrderEntity> orderEntities, boolean ignoreNotFound) {
    // Collect product IDs
    List<Integer> productIds = orderEntities.stream()
        .flatMap(orderEntity -> orderEntity.getOrderItems()
            .stream()
            .map(orderItemEntity -> orderItemEntity.getProduct().getId()))
        .distinct()
        .toList();
    if (CollectionUtils.isEmpty(productIds)) {
      if (ignoreNotFound) {
        return;
      } else {
        throw new DomainException(AppError.PRODUCT_NOT_FOUND);
      }
    }

    // Call product-service to fetch product info
    ListProductRequest request = new ListProductRequest(productIds);
    List<ProductResponse> productResponses = productApiPort.call(request);
    if (CollectionUtils.isEmpty(productResponses)) {
      return;
    }

    // Update order item's product
    Map<Integer, ProductResponse> productResponseMap = productResponses.stream()
        .collect(Collectors.toMap(ProductResponse::getId, Function.identity()));
    orderEntities.forEach(orderEntity -> {
      orderEntity.getOrderItems().forEach(orderItemEntity -> {
        ProductResponse productResponse = productResponseMap.get(
            orderItemEntity.getProduct().getId());
        if (productResponse != null) {
          ProductEntity productEntity = ObjectMapperUtil.getInstance()
              .map(productResponse, ProductEntity.class);
          orderItemEntity.setProduct(productEntity);
        } else {
          if (ignoreNotFound) {
            throw new DomainException(AppError.PRODUCT_NOT_FOUND,
                String.format("Product %d not found", orderItemEntity.getProduct().getId()));
          }
        }
      });
    });
  }
}
