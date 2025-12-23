package org.atlas.application.product.port.repository;

import java.util.List;
import org.atlas.domain.product.entity.Category;

public interface CategoryRepository {

  List<Category> findAll();
}
