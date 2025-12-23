package org.atlas.infrastructure.persistence.jpa.adapter.payment;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.application.payment.port.repository.PaymentGatewayRepository;
import org.atlas.domain.payment.entity.PaymentGateway;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.adapter.payment.entity.JpaPaymentGateway;
import org.atlas.infrastructure.persistence.jpa.adapter.payment.mapper.JpaPaymentGatewayMapper;
import org.atlas.infrastructure.persistence.jpa.adapter.payment.repository.JpaPaymentGatewayRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaPaymentGatewayRepositoryAdapter implements PaymentGatewayRepository {

  private final JpaPaymentGatewayRepository jpaPaymentGatewayRepository;

  @Override
  public List<PaymentGateway> findAll() {
    List<JpaPaymentGateway> jpaPaymentGateways = jpaPaymentGatewayRepository.findAll();
    return ObjectMapperUtil.mapList(jpaPaymentGateways,
        JpaPaymentGatewayMapper.INSTANCE::toPaymentGateway);
  }

  @Override
  public Optional<PaymentGateway> findById(Integer id) {
    return jpaPaymentGatewayRepository.findById(id)
        .map(JpaPaymentGatewayMapper.INSTANCE::toPaymentGateway);
  }

  @Override
  public Optional<PaymentGateway> findByCode(String code) {
    return jpaPaymentGatewayRepository.findByCode(code)
        .map(JpaPaymentGatewayMapper.INSTANCE::toPaymentGateway);
  }
}
