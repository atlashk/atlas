package org.atlas.infrastructure.api.server.grpc.netdevh.impl.product;

import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.atlas.domain.product.entity.Product;
import org.atlas.domain.product.usecase.internal.handler.InternalListProductUseCaseHandler;
import org.atlas.domain.product.usecase.internal.model.InternalListProductInput;
import org.atlas.framework.collection.CollectionUtil;
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
      List<Product> products = internalListProductUseCaseHandler.handle(input);
      ListProductResponseProto productResponseProtoList = map(products);
      responseObserver.onNext(productResponseProtoList);
      responseObserver.onCompleted();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private InternalListProductInput map(ListProductRequestProto requestProto) {
    return new InternalListProductInput(requestProto.getIdList());
  }

  private ListProductResponseProto map(List<Product> products) {
    if (CollectionUtil.isEmpty(products)) {
      return ListProductResponseProto.getDefaultInstance();
    }
    ListProductResponseProto.Builder builder = ListProductResponseProto.newBuilder();
    products.forEach(product -> builder.addProduct(map(product)));
    return builder.build();
  }

  private ProductProto map(Product product) {
    return ProductProto.newBuilder()
        .setId(product.getId())
        .setName(product.getName())
        .setPrice(product.getPrice().doubleValue())
        .build();
  }
}
