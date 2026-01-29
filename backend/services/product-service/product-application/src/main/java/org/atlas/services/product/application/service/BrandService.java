package org.atlas.services.product.application.service;

import java.util.List;
import org.atlas.services.product.domain.entity.Brand;

public interface BrandService {

  List<Brand> retrieveAllBrand();
}
