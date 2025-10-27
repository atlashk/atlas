package org.atlas.infrastructure.persistence.jpa.impl.payment;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.repository.PaymentRepository;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.impl.payment.entity.JpaPaymentEntity;
import org.atlas.infrastructure.persistence.jpa.impl.payment.mapper.JpaPaymentEntityMapper;
import org.atlas.infrastructure.persistence.jpa.impl.payment.repository.JpaPaymentRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaPaymentRepositoryAdapter implements PaymentRepository {

  private final JpaPaymentRepository jpaPaymentRepository;

  @Override
  public List<PaymentEntity> findByOrderIdIn(List<Integer> orderIds) {
    List<JpaPaymentEntity> jpaPaymentEntities = jpaPaymentRepository.findByOrderIdIn(orderIds);
    return ObjectMapperUtil.getInstance()
        .mapList(jpaPaymentEntities, JpaPaymentEntityMapper::toPaymentEntity);
  }

  @Override
  public Optional<PaymentEntity> findById(Integer id) {
    return jpaPaymentRepository.findById(id)
        .map(JpaPaymentEntityMapper::toPaymentEntity);
  }

  @Override
  public Optional<PaymentEntity> findByOrderId(Integer orderId) {
    return jpaPaymentRepository.findByOrderId(orderId)
        .map(JpaPaymentEntityMapper::toPaymentEntity);
  }

  @Override
  public void insert(PaymentEntity payment) {
    JpaPaymentEntity jpaPayment = JpaPaymentEntityMapper.toJpaPaymentEntity(payment);
    jpaPaymentRepository.save(jpaPayment);
    payment.setId(jpaPayment.getId());
  }

  @Override
  public void update(PaymentEntity payment) {
    JpaPaymentEntity jpaPayment = JpaPaymentEntityMapper.toJpaPaymentEntity(payment);
    jpaPaymentRepository.save(jpaPayment);
  }
}
