package org.atlas.domain.product.repository;

import java.util.List;
import org.atlas.domain.product.entity.BrandEntity;

public interface BrandRepository {

  List<BrandEntity> findAll();
}
