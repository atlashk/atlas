package org.atlas.services.catalog.infrastructure.api.server.grpc;

import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.protobuf.catalog.ProductServiceGrpc;
import org.atlas.libs.protobuf.catalog.RetrieveProductListRequestProto;
import org.atlas.libs.protobuf.catalog.RetrieveProductListResponseProto;
import org.atlas.services.catalog.domain.entity.ProductEntity;
import org.atlas.services.catalog.port.in.product.model.internal.RetrieveProductListInput;
import org.atlas.services.catalog.port.in.product.service.ProductInternalService;

@GrpcService
@RequiredArgsConstructor
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

  private final ProductInternalService productInternalService;

  @Override
  public void retrieveProductList(RetrieveProductListRequestProto requestProto,
      StreamObserver<RetrieveProductListResponseProto> responseObserver) {
    RetrieveProductListInput input = map(requestProto);
    try {
      List<ProductEntity> products = productInternalService.retrieveProductList(input);
      ListProductResponseProto productResponseProtoList = map(products);
      responseObserver.onNext(productResponseProtoList);
      responseObserver.onCompleted();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private RetrieveProductListInput map(ListProductRequestProto requestProto) {
    return new RetrieveProductListInput(requestProto.getIdList());
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
        .setId(product.getId())
        .setName(product.getName())
        .setPrice(product.getPrice().doubleValue())
        .build();
  }
}
