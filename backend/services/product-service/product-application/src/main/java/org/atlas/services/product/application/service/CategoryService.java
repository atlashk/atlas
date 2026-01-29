package org.atlas.services.product.application.service;

import java.util.List;
import org.atlas.services.product.domain.entity.Category;

public interface CategoryService {

  List<Category> retrieveAllCategory();
}
