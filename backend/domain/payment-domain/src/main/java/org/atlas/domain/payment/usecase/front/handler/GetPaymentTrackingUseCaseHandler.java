package org.atlas.domain.payment.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.repository.PaymentRepository;
import org.atlas.domain.payment.usecase.front.model.GetPaymentTrackingInput;
import org.atlas.domain.payment.usecase.front.model.GetPaymentTrackingOutput;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;
import org.atlas.framework.objectmapper.ObjectMapperUtil;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class GetPaymentTrackingUseCaseHandler {

  private final PaymentRepository paymentRepository;

  public GetPaymentTrackingOutput handle(GetPaymentTrackingInput input) throws Exception {
    PaymentEntity payment = paymentRepository.findBySagaId(input.getSagaId())
        .orElseThrow(() -> new DomainException(DomainError.PAYMENT_NOT_FOUND));
    return ObjectMapperUtil.getInstance()
        .map(payment, GetPaymentTrackingOutput.class);
  }
}
