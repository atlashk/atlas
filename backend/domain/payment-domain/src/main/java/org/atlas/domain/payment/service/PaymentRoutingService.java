package org.atlas.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.shared.PaymentMethod;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.constant.Application;
import org.atlas.framework.dependency.DependencyPort;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.service.DomainService;
import org.atlas.framework.payment.PaymentGatewayPort;
import org.atlas.framework.util.StringUtil;

@DomainService
@RequiredArgsConstructor
@Slf4j
public class PaymentRoutingService {

  private final ApplicationConfigPort applicationConfigPort;
  private final DependencyPort dependencyPort;

  public PaymentGatewayPort getPaymentGateway(PaymentMethod paymentMethod) {
    // Find the relevant payment gateway from the application config
    String paymentGatewayName = applicationConfigPort.getConfig(Application.PAYMENT_SERVICE,
        "routing." + paymentMethod.getType());
    if (StringUtil.isBlank(paymentGatewayName)) {
      log.error("Payment method {} has not routed yet", paymentMethod);
      throw new DomainException(DomainError.PAYMENT_METHOD_NOT_SUPPORTED);
    }

    // Load payment gateway instance
    String paymentGatewayPortName = String.format("%sPaymentGatewayAdapter", paymentGatewayName);
    return dependencyPort.getInstanceByName(paymentGatewayPortName, PaymentGatewayPort.class)
        .orElseThrow(() -> {
          log.error("Payment gateway {} has not integrated yet", paymentGatewayName);
          return new DomainException(DomainError.PAYMENT_GATEWAY_NOT_SUPPORTED);
        });
  }
}
