package org.atlas.services.catalog.api.grpc;

import io.grpc.stub.StreamObserver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.atlas.libs.framework.internal.catalog.model.ProductOutput;
import org.atlas.libs.framework.internal.catalog.model.RetrieveProductListInput;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.protobuf.catalog.ProductProto;
import org.atlas.libs.protobuf.catalog.ProductServiceGrpc;
import org.atlas.libs.protobuf.catalog.RetrieveProductListRequestProto;
import org.atlas.libs.protobuf.catalog.RetrieveProductListResponseProto;
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
      List<ProductOutput> products = productInternalService.retrieveProductList(input);
      RetrieveProductListResponseProto productResponseProtoList = map(products);
      responseObserver.onNext(productResponseProtoList);
      responseObserver.onCompleted();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private RetrieveProductListInput map(RetrieveProductListRequestProto requestProto) {
    return new RetrieveProductListInput(requestProto.getIdList());
  }

  private RetrieveProductListResponseProto map(List<ProductOutput> products) {
    if (CollectionUtil.isEmpty(products)) {
      return RetrieveProductListResponseProto.getDefaultInstance();
    }
    RetrieveProductListResponseProto.Builder builder = RetrieveProductListResponseProto.newBuilder();
    products.forEach(product -> builder.addProduct(map(product)));
    return builder.build();
  }

  private ProductProto map(ProductOutput product) {
    return ProductProto.newBuilder()
        .setId(product.getId())
        .setName(product.getName())
        .setPrice(product.getPrice().doubleValue())
        .setImage(product.getImage())
        .build();
  }
}
