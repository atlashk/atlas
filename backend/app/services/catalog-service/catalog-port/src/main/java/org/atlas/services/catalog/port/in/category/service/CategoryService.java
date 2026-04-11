package org.atlas.services.catalog.port.in.category.service;

import java.util.List;
import org.atlas.services.catalog.domain.entity.Category;

public interface CategoryService {

  List<Category> retrieveAllCategory();
}
