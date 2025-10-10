package org.atlas.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.shared.PaymentMethod;
import org.atlas.framework.config.ApplicationConfigService;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.paymentgateway.PaymentGatewayService;
import org.atlas.framework.util.StringUtil;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRoutingService {

  private final ApplicationContext applicationContext;
  private final ApplicationConfigService applicationConfigService;

  public PaymentGatewayService getPaymentGateway(PaymentMethod paymentMethod) {
    // Find the relevant payment gateway from the application config
    String paymentGatewayName = applicationConfigService.getConfig(
        "routing." + paymentMethod.getType(), "stripe");
    if (StringUtil.isBlank(paymentGatewayName)) {
      log.error("Payment method {} has not routed yet", paymentMethod);
      throw new DomainException(DomainError.PAYMENT_METHOD_NOT_SUPPORTED);
    }

    // Load payment gateway instance
    String paymentGatewayPortName = String.format("%sPaymentGatewayAdapter", paymentGatewayName);
    try {
      return applicationContext.getBean(paymentGatewayPortName, PaymentGatewayService.class);
    } catch (NoSuchBeanDefinitionException e) {
      throw new DomainException(DomainError.PAYMENT_GATEWAY_NOT_SUPPORTED);
    }
  }
}
