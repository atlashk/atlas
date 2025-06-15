package org.atlas.infrastructure.api.server.grpc.netdevh.adapter.product;

import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.collections4.CollectionUtils;
import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.domain.product.usecase.internal.handler.InternalListProductUseCaseHandler;
import org.atlas.domain.product.usecase.internal.model.InternalListProductInput;
import org.atlas.infrastructure.api.server.grpc.protobuf.product.ListProductRequestProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.product.ListProductResponseProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.product.ProductProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.product.ProductServiceGrpc;

@GrpcService
@RequiredArgsConstructor
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

  private final InternalListProductUseCaseHandler internalListProductUseCaseHandler;

  @Override
  public void listProduct(ListProductRequestProto requestProto,
      StreamObserver<ListProductResponseProto> responseObserver) {
    InternalListProductInput input = map(requestProto);
    try {
      List<ProductEntity> productEntities = internalListProductUseCaseHandler.handle(input);
      ListProductResponseProto productResponseProtoList = map(productEntities);
      responseObserver.onNext(productResponseProtoList);
      responseObserver.onCompleted();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private InternalListProductInput map(ListProductRequestProto requestProto) {
    return new InternalListProductInput(requestProto.getIdList());
  }

  private ListProductResponseProto map(List<ProductEntity> productEntities) {
    if (CollectionUtils.isEmpty(productEntities)) {
      return ListProductResponseProto.getDefaultInstance();
    }
    ListProductResponseProto.Builder builder = ListProductResponseProto.newBuilder();
    productEntities.forEach(productEntity -> builder.addProduct(map(productEntity)));
    return builder.build();
  }

  private ProductProto map(ProductEntity productEntity) {
    return ProductProto.newBuilder()
        .setId(productEntity.getId())
        .setName(productEntity.getName())
        .setPrice(productEntity.getPrice().doubleValue())
        .build();
  }
}
