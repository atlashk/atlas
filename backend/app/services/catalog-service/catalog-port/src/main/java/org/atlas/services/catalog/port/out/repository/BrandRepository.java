package org.atlas.services.catalog.port.out.repository;

import java.util.List;
import org.atlas.services.catalog.domain.entity.BrandEntity;

public interface BrandRepository {

  List<BrandEntity> findAll();
}
