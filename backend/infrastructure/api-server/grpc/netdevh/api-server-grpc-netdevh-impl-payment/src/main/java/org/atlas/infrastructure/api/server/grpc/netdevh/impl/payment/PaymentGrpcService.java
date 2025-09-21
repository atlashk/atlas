package org.atlas.infrastructure.api.server.grpc.netdevh.impl.payment;

import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.atlas.domain.payment.entity.PaymentEntity;
import org.atlas.domain.payment.usecase.internal.handler.InternalListPaymentUseCaseHandler;
import org.atlas.domain.payment.usecase.internal.model.InternalListPaymentInput;
import org.atlas.framework.util.CollectionUtil;
import org.atlas.framework.util.StringUtil;
import org.atlas.infrastructure.api.server.grpc.protobuf.payment.ListPaymentRequestProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.payment.ListPaymentResponseProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.payment.PaymentProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.payment.PaymentServiceGrpc;

@GrpcService
@RequiredArgsConstructor
public class PaymentGrpcService extends PaymentServiceGrpc.PaymentServiceImplBase {

  private final InternalListPaymentUseCaseHandler internalListPaymentUseCaseHandler;

  @Override
  public void listPayment(ListPaymentRequestProto requestProto,
      StreamObserver<ListPaymentResponseProto> responseObserver) {
    InternalListPaymentInput input = map(requestProto);
    try {
      List<PaymentEntity> paymentEntities = internalListPaymentUseCaseHandler.handle(input);
      ListPaymentResponseProto paymentResponseProtoList = map(paymentEntities);
      responseObserver.onNext(paymentResponseProtoList);
      responseObserver.onCompleted();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private InternalListPaymentInput map(ListPaymentRequestProto requestProto) {
    return new InternalListPaymentInput(requestProto.getIdList());
  }

  private ListPaymentResponseProto map(List<PaymentEntity> paymentEntities) {
    if (CollectionUtil.isEmpty(paymentEntities)) {
      return ListPaymentResponseProto.getDefaultInstance();
    }
    ListPaymentResponseProto.Builder builder = ListPaymentResponseProto.newBuilder();
    paymentEntities.forEach(paymentEntity -> builder.addPayment(map(paymentEntity)));
    return builder.build();
  }

  private PaymentProto map(PaymentEntity paymentEntity) {
    return PaymentProto.newBuilder()
        .setId(paymentEntity.getId())
        .setAmount(paymentEntity.getAmount().doubleValue())
        .setCurrency(paymentEntity.getCurrency())
        .setMethod(
            paymentEntity.getMethod() != null ? paymentEntity.getMethod().name() : StringUtil.EMPTY)
        .setGateway(paymentEntity.getGateway() != null ? paymentEntity.getGateway().name()
            : StringUtil.EMPTY)
        .setStatus(
            paymentEntity.getStatus() != null ? paymentEntity.getStatus().name() : StringUtil.EMPTY)
        .setErrorCode(StringUtil.nvl(paymentEntity.getErrorCode()))
        .setErrorMessage(StringUtil.nvl(paymentEntity.getErrorMessage()))
        .setCancellationReason(StringUtil.nvl(paymentEntity.getCancellationReason()))
        .build();
  }
}
