package org.atlas.framework.payment;

import java.math.BigDecimal;
import java.util.Map;
import org.atlas.domain.payment.shared.enums.PaymentGateway;
import org.atlas.domain.payment.shared.enums.PaymentStatus;

public interface PaymentGatewayPort {

  PaymentGateway supports();

  Map<String, Object> createPayment(Integer orderId, Integer userId, BigDecimal amount,
      String currency);

  PaymentStatus getPaymentStatus(String transactionId);

  Map<String, Object> handleWebhook(Map<String, Object> payload, Map<String, String> headers);

  boolean verifySignature(Map<String, Object> payload, Map<String, String> headers);
}
