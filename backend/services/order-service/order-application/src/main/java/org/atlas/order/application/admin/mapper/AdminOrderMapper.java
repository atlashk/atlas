package org.atlas.order.application.admin.mapper;

import org.atlas.order.application.admin.model.AdminRetrieveOrderListInput;
import org.atlas.order.application.port.repository.criteria.FindOrderCriteria;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminOrderMapper {

  AdminOrderMapper INSTANCE = Mappers.getMapper(AdminOrderMapper.class);

  FindOrderCriteria toFindOrderCriteria(AdminRetrieveOrderListInput input);
}
