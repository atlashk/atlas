package org.atlas.application.payment.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.application.payment.port.repository.PaymentGatewayRepository;
import org.atlas.domain.payment.entity.PaymentGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

  private final PaymentGatewayRepository paymentGatewayRepository;

  @Override
  @Transactional(readOnly = true)
  public List<PaymentGateway> retrievePaymentGatewayList() {
    return paymentGatewayRepository.findAll();
  }
}
