package org.atlas.domain.payment.repository;

import java.util.Optional;
import org.atlas.domain.payment.entity.PaymentIntentEntity;

public interface PaymentIntentRepository {

  Optional<PaymentIntentEntity> findById(Integer id);
  
  Optional<PaymentIntentEntity> findByOrderId(Integer orderId);
  
  Optional<PaymentIntentEntity> findByStripePaymentIntentId(String stripePaymentIntentId);
  
  boolean existsByOrderId(Integer orderId);
  
  void insert(PaymentIntentEntity paymentIntent);
  
  void update(PaymentIntentEntity paymentIntent);
  
  void save(PaymentIntentEntity paymentIntentEntity);
}
