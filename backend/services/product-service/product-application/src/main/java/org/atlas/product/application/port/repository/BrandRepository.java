package org.atlas.product.application.port.repository;

import java.util.List;
import org.atlas.product.domain.entity.Brand;

public interface BrandRepository {

  List<Brand> findAll();
}
