package org.atlas.payment.persistence.jpa.adapter;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.payment.application.port.repository.PaymentRepository;
import org.atlas.payment.domain.entity.Payment;
import org.atlas.payment.persistence.jpa.entity.JpaPayment;
import org.atlas.payment.persistence.jpa.mapper.JpaPaymentMapper;
import org.atlas.payment.persistence.jpa.repository.JpaPaymentRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaPaymentRepositoryAdapter implements PaymentRepository {

  private final JpaPaymentRepository jpaPaymentRepository;

  @Override
  public Optional<Payment> findById(Integer id) {
    return jpaPaymentRepository.findById(id)
        .map(JpaPaymentMapper.INSTANCE::toPayment);
  }

  @Override
  public Optional<Payment> findByOrderId(Integer orderId) {
    return jpaPaymentRepository.findByOrderId(orderId)
        .map(JpaPaymentMapper.INSTANCE::toPayment);
  }

  @Override
  public void insert(Payment payment) {
    JpaPayment jpaPayment = JpaPaymentMapper.INSTANCE.toJpaPayment(payment);
    jpaPaymentRepository.insert(jpaPayment);
    payment.setId(jpaPayment.getId());
  }

  @Override
  public void update(Payment payment) {
    JpaPayment jpaPayment = JpaPaymentMapper.INSTANCE.toJpaPayment(payment);
    jpaPaymentRepository.save(jpaPayment);
  }
}
