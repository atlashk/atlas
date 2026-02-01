package org.atlas.services.product.port.in.front.service;

import java.util.List;
import org.atlas.services.product.domain.entity.Category;

public interface CategoryService {

  List<Category> retrieveAllCategory();
}
