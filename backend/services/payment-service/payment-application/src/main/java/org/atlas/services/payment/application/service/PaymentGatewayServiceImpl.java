package org.atlas.services.payment.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.payment.domain.entity.PaymentGatewayEntity;
import org.atlas.services.payment.domain.error.DomainError;
import org.atlas.services.payment.domain.exception.DomainException;
import org.atlas.services.payment.port.in.model.RetrievePaymentGatewayInput;
import org.atlas.services.payment.port.in.service.PaymentGatewayService;
import org.atlas.services.payment.port.out.gateway.service.PaymentGatewayIntegrationService;
import org.atlas.services.payment.port.out.repository.PaymentGatewayRepository;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

  private final PaymentGatewayRepository paymentGatewayRepository;
  private final ApplicationContext applicationContext;

  @Override
  @Transactional(readOnly = true)
  public List<PaymentGatewayEntity> retrievePaymentGatewayList() {
    return paymentGatewayRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentGatewayEntity retrievePaymentGateway(RetrievePaymentGatewayInput input) {
    if (input.getId() != null) {
      return paymentGatewayRepository.findById(input.getId())
          .orElseThrow(() -> new DomainException(DomainError.PAYMENT_GATEWAY_NOT_FOUND));
    } else if (StringUtil.isNotBlank(input.getCode())) {
      return paymentGatewayRepository.findByCode(input.getCode().toUpperCase())
          .orElseThrow(() -> new DomainException(DomainError.PAYMENT_GATEWAY_NOT_FOUND));
    } else {
      throw new DomainException(DomainError.PAYMENT_GATEWAY_NOT_FOUND);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentGatewayIntegrationService retrievePaymentGatewayIntegrationService(
      PaymentGatewayEntity paymentGateway) {
    String paymentGatewayIntegrationServiceBeanName =
        String.format("%sIntegrationService", paymentGateway.getCode().toLowerCase());
    try {
      return applicationContext.getBean(
          paymentGatewayIntegrationServiceBeanName, PaymentGatewayIntegrationService.class);
    } catch (NoSuchBeanDefinitionException e) {
      throw new DomainException(DomainError.PAYMENT_GATEWAY_NOT_FOUND);
    }
  }
}
