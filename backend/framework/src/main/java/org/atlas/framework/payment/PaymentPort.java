package org.atlas.framework.payment;

import java.util.Map;

public interface PaymentPort {

  Map<String, Object> createPayment(Integer orderId, Integer userId, String amount,
      String currency);
}
