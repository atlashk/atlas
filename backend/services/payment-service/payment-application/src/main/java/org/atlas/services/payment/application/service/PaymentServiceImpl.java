package org.atlas.services.payment.application.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.domain.payment.PaymentStatus;
import org.atlas.services.payment.application.mapper.PaymentMapper;
import org.atlas.services.payment.port.in.model.RetrievePaymentNextActionOutput;
import org.atlas.services.payment.port.in.service.PaymentService;
import org.atlas.services.payment.port.out.repository.PaymentRepository;
import org.atlas.services.payment.domain.entity.PaymentEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;

  @Override
  @Transactional(readOnly = true)
  public RetrievePaymentNextActionOutput retrievePaymentNextAction(String orderId) {
    String userId = Contexts.getUserId();

    PaymentEntity payment = paymentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new DomainException(DomainError.PAYMENT_NOT_FOUND));

    if (!PaymentStatus.CREATED.equals(payment.getStatus())) {
      log.error("The expected payment status is CREATED: {}", payment.getStatus());
      throw new DomainException(DomainError.INVALID_PAYMENT_STATUS);
    }

    if (!Objects.equals(userId, payment.getUserId())) {
      throw new DomainException(DomainError.FORBIDDEN);
    }

    return PaymentMapper.INSTANCE.toRetrievePaymentNextActionOutput(payment);
  }
}
