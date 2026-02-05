package org.atlas.services.product.infrastructure.api.server.grpc;

import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.atlas.libs.protobuf.product.ListProductRequestProto;
import org.atlas.libs.protobuf.product.ListProductResponseProto;
import org.atlas.libs.protobuf.product.ProductProto;
import org.atlas.libs.protobuf.product.ProductServiceGrpc;
import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.port.in.internal.model.InternalRetrieveProductListInput;
import org.atlas.services.product.port.in.internal.service.InternalProductService;

@GrpcService
@RequiredArgsConstructor
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

  private final InternalProductService internalProductService;

  @Override
  public void listProduct(ListProductRequestProto requestProto,
      StreamObserver<ListProductResponseProto> responseObserver) {
    InternalRetrieveProductListInput input = map(requestProto);
    try {
      List<ProductEntity> products = internalProductService.retrieveProductList(input);
      ListProductResponseProto productResponseProtoList = map(products);
      responseObserver.onNext(productResponseProtoList);
      responseObserver.onCompleted();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private InternalRetrieveProductListInput map(ListProductRequestProto requestProto) {
    return new InternalRetrieveProductListInput(requestProto.getProductIdList());
  }

  private ListProductResponseProto map(List<ProductEntity> products) {
    if (CollectionUtil.isEmpty(products)) {
      return ListProductResponseProto.getDefaultInstance();
    }
    ListProductResponseProto.Builder builder = ListProductResponseProto.newBuilder();
    products.forEach(product -> builder.addProduct(map(product)));
    return builder.build();
  }

  private ProductProto map(ProductEntity product) {
    return ProductProto.newBuilder()
        .setProductId(product.getProductId())
        .setName(product.getName())
        .setPrice(product.getPrice().doubleValue())
        .build();
  }
}
