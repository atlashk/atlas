package org.atlas.services.catalog.port.in.brand.service;

import java.util.List;
import org.atlas.services.catalog.domain.entity.Brand;

public interface BrandService {

  List<Brand> retrieveAllBrand();
}
