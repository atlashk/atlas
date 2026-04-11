package org.atlas.services.catalog.port.out.repository;

import java.util.List;
import org.atlas.services.catalog.domain.entity.Category;

public interface CategoryRepository {

  List<Category> findAll();
}
