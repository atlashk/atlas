package org.atlas.product.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.product.application.port.repository.CategoryRepository;
import org.atlas.product.domain.entity.Category;
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
