package org.atlas.infrastructure.persistence.jpa.adapter.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.application.payment.port.repository.PaymentEventRepository;
import org.atlas.domain.payment.entity.PaymentEvent;
import org.atlas.infrastructure.persistence.jpa.adapter.payment.entity.JpaPaymentEvent;
import org.atlas.infrastructure.persistence.jpa.adapter.payment.mapper.JpaPaymentEventMapper;
import org.atlas.infrastructure.persistence.jpa.adapter.payment.repository.JpaPaymentEventRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaPaymentEventRepositoryAdapter implements PaymentEventRepository {

  private final JpaPaymentEventRepository jpaPaymentEventRepository;

  @Override
  public void insert(PaymentEvent paymentEvent) {
    JpaPaymentEvent jpaPaymentEvent =
        JpaPaymentEventMapper.INSTANCE.toJpaPaymentEvent(paymentEvent);
    jpaPaymentEventRepository.insert(jpaPaymentEvent);
    paymentEvent.setId(jpaPaymentEvent.getId());
  }

  @Override
  public void update(PaymentEvent paymentEvent) {
    JpaPaymentEvent jpaPaymentEvent =
        JpaPaymentEventMapper.INSTANCE.toJpaPaymentEvent(paymentEvent);
    jpaPaymentEventRepository.save(jpaPaymentEvent);
  }
}
