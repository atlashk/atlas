package org.atlas.product.application.port.repository;

import java.util.List;
import org.atlas.product.domain.entity.Category;

public interface CategoryRepository {

  List<Category> findAll();
}
