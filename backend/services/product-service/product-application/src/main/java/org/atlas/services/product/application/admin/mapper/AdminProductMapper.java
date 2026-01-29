package org.atlas.services.product.application.admin.mapper;

import org.atlas.services.product.application.admin.model.AdminExportProductInput;
import org.atlas.services.product.application.admin.model.AdminRetrieveProductListInput;
import org.atlas.services.product.application.port.repository.criteria.FindProductCriteria;
import org.atlas.services.product.domain.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminProductMapper {

  AdminProductMapper INSTANCE = Mappers.getMapper(AdminProductMapper.class);

  FindProductCriteria toFindProductCriteria(AdminRetrieveProductListInput input);

  FindProductCriteria toFindProductCriteria(AdminExportProductInput input);

  void merge(Product source, @MappingTarget Product target);
}
