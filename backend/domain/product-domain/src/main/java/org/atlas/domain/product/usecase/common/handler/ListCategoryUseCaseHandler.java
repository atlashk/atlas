package org.atlas.domain.product.usecase.common.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.product.entity.CategoryEntity;
import org.atlas.domain.product.repository.CategoryRepository;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class ListCategoryUseCaseHandler {

  private final CategoryRepository categoryRepository;

  public List<CategoryEntity> handle(Void input) throws Exception {
    return categoryRepository.findAll();
  }
}
