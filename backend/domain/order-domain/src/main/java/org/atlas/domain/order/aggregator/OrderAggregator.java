package org.atlas.domain.order.aggregator;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.entity.OrderEntity;
import org.atlas.domain.order.entity.PaymentEntity;
import org.atlas.domain.order.entity.ProductEntity;
import org.atlas.domain.order.entity.UserEntity;
import org.atlas.framework.internalapi.payment.PaymentApiPort;
import org.atlas.framework.internalapi.payment.model.ListPaymentRequest;
import org.atlas.framework.internalapi.payment.model.PaymentResponse;
import org.atlas.framework.internalapi.product.ProductApiPort;
import org.atlas.framework.internalapi.product.model.ListProductRequest;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.atlas.framework.internalapi.user.UserApiPort;
import org.atlas.framework.internalapi.user.model.ListUserRequest;
import org.atlas.framework.internalapi.user.model.UserResponse;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.util.ArrayUtil;
import org.atlas.framework.util.CollectionUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderAggregator {

  private final UserApiPort userApiPort;
  private final ProductApiPort productApiPort;
  private final PaymentApiPort paymentApiPort;

  /**
   * Aggregate specific data types for a single order entity
   */
  public void aggregate(OrderEntity order, AggregationOptions... options) {
    if (order == null) {
      throw new IllegalArgumentException("Order must be provided");
    }
    aggregate(List.of(order), options);
  }

  /**
   * Aggregate specific data types for multiple order entities
   */
  public void aggregate(List<OrderEntity> orderEntities, AggregationOptions... options) {
    if (CollectionUtil.isEmpty(orderEntities) || ArrayUtil.isEmpty(options)) {
      throw new IllegalArgumentException("Orders must be provided");
    }

    if (options[0].isLoadUsers()) {
      loadUsers(orderEntities);
    }
    if (options[0].isLoadProducts()) {
      loadProducts(orderEntities);
    }
    if (options[0].isLoadPayments()) {
      loadPayments(orderEntities);
    }
  }

  private void loadUsers(List<OrderEntity> orderEntities) {
    // Collect user IDs
    List<Integer> userIds = orderEntities.stream()
        .map(orderEntity -> orderEntity.getUser().getId())
        .distinct()
        .toList();
    if (CollectionUtil.isEmpty(userIds)) {
      return;
    }

    // Call user-service to fetch user info
    ListUserRequest request = new ListUserRequest(userIds);
    List<UserResponse> userResponses = userApiPort.call(request);
    if (CollectionUtil.isEmpty(userResponses)) {
      return; // Skip if no users found
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
      }
      // Skip if user not found instead of throwing exception
    });
  }

  private void loadProducts(List<OrderEntity> orderEntities) {
    // Collect product IDs
    List<Integer> productIds = orderEntities.stream()
        .flatMap(orderEntity -> orderEntity.getOrderItems()
            .stream()
            .map(orderItemEntity -> orderItemEntity.getProduct().getId()))
        .distinct()
        .toList();
    if (CollectionUtil.isEmpty(productIds)) {
      return;
    }

    // Call product-service to fetch product info
    ListProductRequest request = new ListProductRequest(productIds);
    List<ProductResponse> productResponses = productApiPort.call(request);
    if (CollectionUtil.isEmpty(productResponses)) {
      return; // Skip if no products found
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
        }
        // Skip if product not found instead of throwing exception
      });
    });
  }

  private void loadPayments(List<OrderEntity> orderEntities) {
    // Collect payment IDs
    List<Integer> paymentIds = orderEntities.stream()
        .map(orderEntity -> orderEntity.getPayment().getId())
        .distinct()
        .toList();
    if (CollectionUtil.isEmpty(paymentIds)) {
      return;
    }

    // Call payment-service to fetch payment info by order IDs
    ListPaymentRequest request = ListPaymentRequest.builder()
        .paymentIds(paymentIds)
        .build();
    List<PaymentResponse> paymentResponses = paymentApiPort.call(request);
    if (CollectionUtil.isEmpty(paymentResponses)) {
      return; // Skip if no payments found
    }

    // Create a map of orderId to PaymentResponse
    Map<Integer, PaymentResponse> paymentResponseMap = paymentResponses.stream()
        .collect(Collectors.toMap(PaymentResponse::getId, Function.identity()));

    // Update order's payment
    orderEntities.forEach(orderEntity -> {
      PaymentResponse paymentResponse = paymentResponseMap.get(orderEntity.getPayment().getId());
      if (paymentResponse != null) {
        PaymentEntity paymentEntity = ObjectMapperUtil.getInstance()
            .map(paymentResponse, PaymentEntity.class);
        orderEntity.setPayment(paymentEntity);
      }
      // Skip if payment not found instead of throwing exception
    });
  }

  @Getter
  @Builder
  public static class AggregationOptions {

    private boolean loadUsers;
    private boolean loadProducts;
    private boolean loadPayments;
  }
}
