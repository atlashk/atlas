package org.atlas.domain.order.usecase.admin.mapper;

import org.atlas.domain.order.repository.criteria.FindOrderCriteria;
import org.atlas.domain.order.usecase.admin.model.AdminListOrderInput;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdminOrderMapper {

  AdminOrderMapper INSTANCE = Mappers.getMapper(AdminOrderMapper.class);

  FindOrderCriteria toFindOrderCriteria(AdminListOrderInput input);
}
