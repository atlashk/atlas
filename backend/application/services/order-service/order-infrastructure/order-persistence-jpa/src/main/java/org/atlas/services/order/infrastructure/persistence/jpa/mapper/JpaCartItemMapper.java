package org.atlas.services.order.infrastructure.persistence.jpa.mapper;

import java.util.List;
import org.atlas.services.order.domain.entity.CartItemEntity;
import org.atlas.services.order.infrastructure.persistence.jpa.entity.JpaCartItemEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface JpaCartItemMapper {

  JpaCartItemMapper INSTANCE = Mappers.getMapper(JpaCartItemMapper.class);

  @Mapping(target = "product.id", source = "jpaCartItem.productId")
  @Mapping(target = "id", source = "jpaCartItem.id")
  @Mapping(target = "quantity", source = "jpaCartItem.quantity")
  @Mapping(target = "userId", source = "userId")
  CartItemEntity toCartItem(String userId, JpaCartItemEntity jpaCartItem);

  default List<CartItemEntity> toCartItems(String userId, List<JpaCartItemEntity> jpaCartItems) {
    return jpaCartItems.stream()
        .map(jpaCartItem -> toCartItem(userId, jpaCartItem))
        .toList();
  }
}
