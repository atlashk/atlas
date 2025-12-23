package org.atlas.application.product.service;

import java.util.List;
import org.atlas.domain.product.entity.Category;

public interface CategoryService {

  List<Category> retrieveAllCategory();
}
