package org.atlas.services.product.port.out.repository;

import java.util.List;
import org.atlas.services.product.domain.entity.Category;

public interface CategoryRepository {

  List<Category> findAll();
}
