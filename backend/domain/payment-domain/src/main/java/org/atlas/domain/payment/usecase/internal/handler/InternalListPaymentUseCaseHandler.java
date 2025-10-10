package org.atlas.domain.payment.usecase.internal.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.repository.PaymentRepository;
import org.atlas.domain.payment.usecase.internal.model.InternalListPaymentInput;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class InternalListPaymentUseCaseHandler {

  private final PaymentRepository paymentRepository;

  public List<PaymentEntity> handle(InternalListPaymentInput input) throws Exception {
    return paymentRepository.findByOrderIdIn(input.getOrderIds());
  }
}
