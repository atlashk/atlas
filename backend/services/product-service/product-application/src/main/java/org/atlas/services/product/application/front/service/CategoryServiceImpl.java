package org.atlas.services.product.application.front.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.services.product.port.in.front.service.CategoryService;
import org.atlas.services.product.port.out.repository.CategoryRepository;
import org.atlas.services.product.domain.entity.Category;
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
