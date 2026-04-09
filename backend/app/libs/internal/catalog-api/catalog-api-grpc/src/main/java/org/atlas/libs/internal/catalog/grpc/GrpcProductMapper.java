package org.atlas.libs.internal.catalog.grpc;

import java.util.List;
import org.atlas.libs.framework.internal.catalog.model.ProductOutput;
import org.atlas.libs.framework.internal.catalog.model.RetrieveProductListInput;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.libs.protobuf.catalog.ProductProto;
import org.atlas.libs.protobuf.catalog.RetrieveProductListRequestProto;
import org.atlas.libs.protobuf.catalog.RetrieveProductListResponseProto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GrpcProductMapper {

  GrpcProductMapper INSTANCE = Mappers.getMapper(GrpcProductMapper.class);

  default RetrieveProductListRequestProto map(RetrieveProductListInput request) {
    if (request == null) {
      return null;
    }

    RetrieveProductListRequestProto.Builder builder = RetrieveProductListRequestProto.newBuilder();
    if (CollectionUtil.isNotEmpty(request.getIds())) {
      builder.addAllId(request.getIds());
    }
    return builder.build();
  }

  default List<ProductOutput> map(RetrieveProductListResponseProto responseProto) {
    return MapperUtil.mapList(responseProto.getProductList(), this::map);
  }

  ProductOutput map(ProductProto productProto);
}
