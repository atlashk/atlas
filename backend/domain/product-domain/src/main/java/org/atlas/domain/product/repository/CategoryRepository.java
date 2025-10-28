package org.atlas.domain.product.repository;

import java.util.List;
import org.atlas.domain.product.entity.Category;

public interface CategoryRepository {

  List<Category> findAll();
}
