package org.atlas.services.payment.application.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.libs.framework.domain.shared.payment.PaymentStatus;
import org.atlas.libs.framework.security.SecurityContextUtil;
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.services.payment.application.mapper.PaymentMapper;
import org.atlas.services.payment.domain.entity.PaymentEntity;
import org.atlas.services.payment.domain.error.PaymentDomainError;
import org.atlas.services.payment.port.in.model.CreatePaymentInput;
import org.atlas.services.payment.port.in.model.RetrievePaymentNextActionOutput;
import org.atlas.services.payment.port.in.model.UpdatePaymentInput;
import org.atlas.services.payment.port.in.service.PaymentService;
import org.atlas.services.payment.port.out.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final SequenceGenerator sequenceGenerator;

  @Override
  public PaymentEntity retrievePayment(String id) {
    return paymentRepository.findById(id)
        .orElseThrow(() -> new DomainException(PaymentDomainError.PAYMENT_NOT_FOUND));
  }

  @Override
  @Transactional
  public String createPayment(CreatePaymentInput input) {
    PaymentEntity payment = PaymentMapper.INSTANCE.toPayment(input);
    payment.setId(sequenceGenerator.generate(SequenceType.PAYMENT));
    paymentRepository.insert(payment);
    return payment.getId();
  }

  @Override
  @Transactional
  public void updatePayment(UpdatePaymentInput input) {
    PaymentEntity payment = paymentRepository.findById(input.getId())
        .orElseThrow(() -> new DomainException(PaymentDomainError.PAYMENT_NOT_FOUND));
    PaymentMapper.INSTANCE.merge(input, payment);
    paymentRepository.update(payment);
  }

  @Override
  @Transactional(readOnly = true)
  public RetrievePaymentNextActionOutput retrievePaymentNextAction(String orderId) {
    String userId = SecurityContextUtil.requirePrincipal().getUserId();

    PaymentEntity payment = paymentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new DomainException(PaymentDomainError.PAYMENT_NOT_FOUND));

    if (!PaymentStatus.CREATED.equals(payment.getStatus())) {
      log.error("The expected payment status is CREATED: {}", payment.getStatus());
      throw new DomainException(PaymentDomainError.INVALID_PAYMENT_STATUS);
    }

    if (!Objects.equals(userId, payment.getUserId())) {
      throw new DomainException(CommonDomainError.FORBIDDEN);
    }

    return PaymentMapper.INSTANCE.toRetrievePaymentNextActionOutput(payment);
  }
}
