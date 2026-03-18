package org.atlas.services.payment.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.services.payment.application.mapper.PaymentEventMapper;
import org.atlas.services.payment.domain.entity.PaymentEventEntity;
import org.atlas.services.payment.domain.error.PaymentDomainError;
import org.atlas.services.payment.port.in.model.CreatePaymentEventInput;
import org.atlas.services.payment.port.in.model.UpdatePaymentEventInput;
import org.atlas.services.payment.port.in.service.PaymentEventService;
import org.atlas.services.payment.port.out.repository.PaymentEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventServiceImpl implements PaymentEventService {

  private final PaymentEventRepository paymentEventRepository;

  @Override
  @Transactional
  public Integer createPaymentEvent(CreatePaymentEventInput input) {
    PaymentEventEntity paymentEvent = PaymentEventMapper.INSTANCE.toPaymentEvent(input);
    paymentEventRepository.insert(paymentEvent);
    return paymentEvent.getId();
  }

  @Override
  @Transactional
  public void updatePaymentEvent(UpdatePaymentEventInput input) {
    PaymentEventEntity payment = paymentEventRepository.findById(input.getId())
        .orElseThrow(() -> new DomainException(PaymentDomainError.PAYMENT_NOT_FOUND));
    PaymentEventMapper.INSTANCE.merge(input, payment);
    paymentEventRepository.update(payment);
  }
}
