package org.atlas.application.product.port.repository;

import java.util.List;
import org.atlas.domain.product.entity.Brand;

public interface BrandRepository {

  List<Brand> findAll();
}
