package org.atlas.domain.payment.usecase.front.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.PaymentGateway;
import org.atlas.domain.payment.repository.PaymentGatewayRepository;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class ListPaymentGatewayUseCaseHandler {

  private final PaymentGatewayRepository paymentGatewayRepository;

  public List<PaymentGateway> handle() throws Exception {
    return paymentGatewayRepository.findAll();
  }
}
