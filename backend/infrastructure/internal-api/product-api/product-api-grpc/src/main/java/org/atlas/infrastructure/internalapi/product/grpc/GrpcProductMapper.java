package org.atlas.infrastructure.internalapi.product.grpc;

import java.util.List;
import org.atlas.framework.internalapi.product.model.ListProductRequest;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.grpc.protobuf.product.ListProductRequestProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.product.ListProductResponseProto;
import org.atlas.infrastructure.api.server.grpc.protobuf.product.ProductProto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GrpcProductMapper {

  GrpcProductMapper INSTANCE = Mappers.getMapper(GrpcProductMapper.class);

  /**
   * Maps ListProductRequest to ListProductRequestProto
   */
  @Mapping(source = "ids", target = "idList")
  ListProductRequestProto map(ListProductRequest request);

  /**
   * Maps ListProductResponseProto to List of ProductResponse
   */
  default List<ProductResponse> map(ListProductResponseProto responseProto) {
    return ObjectMapperUtil.mapList(responseProto.getProductList(), this::map);
  }

  /**
   * Maps ProductProto to ProductResponse
   */
  ProductResponse map(ProductProto productProto);
}