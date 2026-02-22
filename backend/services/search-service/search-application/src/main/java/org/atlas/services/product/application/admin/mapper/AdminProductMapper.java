package org.atlas.services.product.application.admin.mapper;

import org.atlas.services.product.domain.entity.ProductEntity;
import org.atlas.services.product.port.in.admin.model.AdminExportProductInput;
import org.atlas.services.product.port.in.admin.model.AdminRetrieveProductListInput;
import org.atlas.services.product.port.out.repository.ProductRepository;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminProductMapper {

  AdminProductMapper INSTANCE = Mappers.getMapper(AdminProductMapper.class);

  ProductRepository.FindProductCriteria toFindProductCriteria(AdminRetrieveProductListInput input);

  ProductRepository.FindProductCriteria toFindProductCriteria(AdminExportProductInput input);

  void merge(ProductEntity source, @MappingTarget ProductEntity target);
}
