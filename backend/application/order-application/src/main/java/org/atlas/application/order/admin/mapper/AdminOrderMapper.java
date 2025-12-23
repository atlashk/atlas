package org.atlas.application.order.admin.mapper;

import org.atlas.application.order.admin.model.AdminRetrieveOrderListInput;
import org.atlas.application.order.port.repository.criteria.FindOrderCriteria;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminOrderMapper {

  AdminOrderMapper INSTANCE = Mappers.getMapper(AdminOrderMapper.class);

  FindOrderCriteria toFindOrderCriteria(AdminRetrieveOrderListInput input);
}
