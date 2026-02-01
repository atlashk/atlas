package org.atlas.services.payment.infrastructure.persistence.jpa.adapter;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.services.payment.domain.entity.PaymentGateway;
import org.atlas.services.payment.infrastructure.persistence.jpa.entity.JpaPaymentGateway;
import org.atlas.services.payment.infrastructure.persistence.jpa.mapper.JpaPaymentGatewayMapper;
import org.atlas.services.payment.infrastructure.persistence.jpa.repository.JpaPaymentGatewayRepository;
import org.atlas.services.payment.port.out.repository.PaymentGatewayRepository;
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
