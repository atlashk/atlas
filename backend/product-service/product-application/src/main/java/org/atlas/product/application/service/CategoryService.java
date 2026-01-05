package org.atlas.product.application.service;

import java.util.List;
import org.atlas.product.domain.entity.Category;

public interface CategoryService {

  List<Category> retrieveAllCategory();
}
