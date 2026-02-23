package org.atlas.services.product.application.admin.mapper;

import org.atlas.services.inventory.domain.entity.StockEntity;
import org.atlas.services.product.port.in.admin.model.AdminExportProductInput;
import org.atlas.services.product.port.in.admin.model.AdminRetrieveProductListInput;
import org.atlas.services.inventory.port.out.repository.StockRepository;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminProductMapper {

  AdminProductMapper INSTANCE = Mappers.getMapper(AdminProductMapper.class);

  StockRepository.FindProductCriteria toFindProductCriteria(AdminRetrieveProductListInput input);

  StockRepository.FindProductCriteria toFindProductCriteria(AdminExportProductInput input);

  void merge(StockEntity source, @MappingTarget StockEntity target);
}
