package org.atlas.application.product.admin.mapper;

import org.atlas.application.product.admin.model.AdminExportProductInput;
import org.atlas.application.product.admin.model.AdminRetrieveProductListInput;
import org.atlas.application.product.port.repository.criteria.FindProductCriteria;
import org.atlas.domain.product.entity.Product;
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
