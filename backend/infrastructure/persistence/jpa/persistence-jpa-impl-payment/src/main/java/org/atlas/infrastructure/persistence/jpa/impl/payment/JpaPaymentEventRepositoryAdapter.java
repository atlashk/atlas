package org.atlas.infrastructure.persistence.jpa.impl.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.PaymentEvent;
import org.atlas.domain.payment.repository.PaymentEventRepository;
import org.atlas.infrastructure.persistence.jpa.impl.payment.entity.JpaPaymentEvent;
import org.atlas.infrastructure.persistence.jpa.impl.payment.mapper.JpaPaymentEventMapper;
import org.atlas.infrastructure.persistence.jpa.impl.payment.repository.JpaPaymentEventRepository;
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
