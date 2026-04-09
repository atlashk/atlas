package org.atlas.libs.internal.catalog.grpc;

import java.util.List;
import org.atlas.libs.framework.internal.catalog.client.ProductApiClient;
import org.atlas.libs.framework.internal.catalog.model.ProductOutput;
import org.atlas.libs.framework.internal.catalog.model.RetrieveProductListInput;
import org.atlas.libs.protobuf.catalog.ProductServiceGrpc;
import org.atlas.libs.protobuf.catalog.RetrieveProductListRequestProto;
import org.atlas.libs.protobuf.catalog.RetrieveProductListResponseProto;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Component;

@Component
public class GrpcProductApiClient implements ProductApiClient {

  private final ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub;

  public GrpcProductApiClient(GrpcChannelFactory channels) {
    this.productServiceBlockingStub =
        ProductServiceGrpc.newBlockingStub(channels.createChannel("catalog-service"));
  }

  @Override
  public List<ProductOutput> call(RetrieveProductListInput request) {
    RetrieveProductListRequestProto requestProto = GrpcProductMapper.INSTANCE.map(request);
    RetrieveProductListResponseProto responseProto =
        productServiceBlockingStub.retrieveProductList(requestProto);
    return GrpcProductMapper.INSTANCE.map(responseProto);
  }
}
