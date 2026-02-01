package org.atlas.services.payment.infrastructure.persistence.jpa.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.services.payment.domain.entity.PaymentEvent;
import org.atlas.services.payment.infrastructure.persistence.jpa.entity.JpaPaymentEvent;
import org.atlas.services.payment.infrastructure.persistence.jpa.mapper.JpaPaymentEventMapper;
import org.atlas.services.payment.infrastructure.persistence.jpa.repository.JpaPaymentEventRepository;
import org.atlas.services.payment.port.out.repository.PaymentEventRepository;
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
