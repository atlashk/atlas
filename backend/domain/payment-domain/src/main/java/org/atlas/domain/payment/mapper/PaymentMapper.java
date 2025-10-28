package org.atlas.domain.payment.mapper;

import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.usecase.front.model.GetPaymentNextActionOutput;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PaymentMapper {

  PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

  GetPaymentNextActionOutput toGetPaymentNextActionOutput(PaymentEntity payment);
}
