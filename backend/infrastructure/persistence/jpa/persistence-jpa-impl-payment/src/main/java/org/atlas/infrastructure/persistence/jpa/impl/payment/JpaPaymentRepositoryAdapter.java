package org.atlas.infrastructure.persistence.jpa.impl.payment;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.repository.PaymentRepository;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.impl.payment.entity.JpaPaymentEntity;
import org.atlas.infrastructure.persistence.jpa.impl.payment.repository.JpaPaymentRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaPaymentRepositoryAdapter implements PaymentRepository {

  private final JpaPaymentRepository jpaPaymentRepository;

  @Override
  public List<PaymentEntity> findByIdIn(List<Integer> ids) {
    List<JpaPaymentEntity> jpaPaymentEntities = jpaPaymentRepository.findAllById(ids);
    return ObjectMapperUtil.getInstance()
        .mapList(jpaPaymentEntities, PaymentEntity.class);
  }

  @Override
  public Optional<PaymentEntity> findById(Integer id) {
    return jpaPaymentRepository.findById(id)
        .map(jpaPaymentEntity -> ObjectMapperUtil.getInstance()
            .map(jpaPaymentEntity, PaymentEntity.class));
  }

  @Override
  public void insert(PaymentEntity paymentEntity) {
    JpaPaymentEntity jpaPaymentEntity = ObjectMapperUtil.getInstance()
        .map(paymentEntity, JpaPaymentEntity.class);
    jpaPaymentRepository.save(jpaPaymentEntity);
    paymentEntity.setId(jpaPaymentEntity.getId());
  }

  @Override
  public void update(PaymentEntity paymentEntity) {
    JpaPaymentEntity jpaPaymentEntity = ObjectMapperUtil.getInstance()
        .map(paymentEntity, JpaPaymentEntity.class);
    jpaPaymentRepository.save(jpaPaymentEntity);
  }
}
