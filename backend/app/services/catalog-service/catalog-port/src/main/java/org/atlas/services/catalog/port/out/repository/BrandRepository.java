package org.atlas.services.catalog.port.out.repository;

import java.util.List;
import org.atlas.services.catalog.domain.entity.Brand;

public interface BrandRepository {

  List<Brand> findAll();
}
