package org.atlas.domain.product.repository;

import java.util.List;
import org.atlas.domain.product.entity.CategoryEntity;

public interface CategoryRepository {

  List<CategoryEntity> findAll();
}
