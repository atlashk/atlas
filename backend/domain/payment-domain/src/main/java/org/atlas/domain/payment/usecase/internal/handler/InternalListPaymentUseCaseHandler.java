package org.atlas.domain.payment.usecase.internal.handler;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.repository.PaymentRepository;
import org.atlas.domain.payment.usecase.internal.model.InternalListPaymentInput;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.util.CollectionUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class InternalListPaymentUseCaseHandler {

  private final PaymentRepository paymentRepository;

  public List<PaymentEntity> handle(InternalListPaymentInput input) throws Exception {
    List<PaymentEntity> paymentEntities = paymentRepository.findByIdIn(input.getIds());
    if (CollectionUtil.isEmpty(paymentEntities)) {
      return Collections.emptyList();
    }
    return paymentEntities;
  }
}
