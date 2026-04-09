package org.atlas.services.payment.infrastructure.persistence.jpa.adapter;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.services.payment.domain.entity.PaymentEntity;
import org.atlas.services.payment.infrastructure.persistence.jpa.entity.JpaPaymentEntity;
import org.atlas.services.payment.infrastructure.persistence.jpa.mapper.JpaPaymentMapper;
import org.atlas.services.payment.infrastructure.persistence.jpa.repository.JpaPaymentRepository;
import org.atlas.services.payment.port.out.repository.PaymentRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaPaymentRepositoryAdapter implements PaymentRepository {

  private final JpaPaymentRepository jpaPaymentRepository;

  @Override
  public Optional<PaymentEntity> findById(String id) {
    return jpaPaymentRepository.findById(id)
        .map(JpaPaymentMapper.INSTANCE::toPayment);
  }

  @Override
  public Optional<PaymentEntity> findByOrderId(String orderId) {
    return jpaPaymentRepository.findByOrderId(orderId)
        .map(JpaPaymentMapper.INSTANCE::toPayment);
  }

  @Override
  public void insert(PaymentEntity payment) {
    JpaPaymentEntity jpaPayment = JpaPaymentMapper.INSTANCE.toJpaPayment(payment);
    jpaPaymentRepository.insert(jpaPayment);
    payment.setId(jpaPayment.getId());
  }

  @Override
  public void update(PaymentEntity payment) {
    JpaPaymentEntity jpaPayment = JpaPaymentMapper.INSTANCE.toJpaPayment(payment);
    jpaPaymentRepository.save(jpaPayment);
  }
}
