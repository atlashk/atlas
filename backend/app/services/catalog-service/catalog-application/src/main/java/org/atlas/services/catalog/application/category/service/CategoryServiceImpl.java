package org.atlas.services.catalog.application.category.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.services.catalog.domain.entity.Category;
import org.atlas.services.catalog.port.in.category.service.CategoryService;
import org.atlas.services.catalog.port.out.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Category> retrieveAllCategory() {
    return categoryRepository.findAll();
  }
}
