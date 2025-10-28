package org.atlas.domain.payment.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.mapper.PaymentMapper;
import org.atlas.domain.payment.repository.PaymentRepository;
import org.atlas.domain.payment.shared.PaymentStatus;
import org.atlas.domain.payment.usecase.front.model.GetPaymentNextActionInput;
import org.atlas.domain.payment.usecase.front.model.GetPaymentNextActionOutput;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class GetPaymentNextActionUseCaseHandler {

  private final PaymentRepository paymentRepository;

  public GetPaymentNextActionOutput handle(GetPaymentNextActionInput input) throws Exception {
    PaymentEntity payment = paymentRepository.findByOrderId(input.getOrderId())
        .orElseThrow(() -> new DomainException(DomainError.PAYMENT_NOT_FOUND));

    if (!PaymentStatus.CREATED.equals(payment.getStatus())) {
      log.error("The expected payment status is CREATED: {}", payment.getStatus());
      throw new DomainException(DomainError.INVALID_PAYMENT_STATUS);
    }

    return PaymentMapper.INSTANCE.toGetPaymentNextActionOutput(payment);
  }
}
