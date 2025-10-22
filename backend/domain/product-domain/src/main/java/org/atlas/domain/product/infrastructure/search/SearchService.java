package org.atlas.domain.product.infrastructure.search;

import org.atlas.domain.product.entity.ProductEntity;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;

public interface SearchService {

  PagingResult<ProductEntity> search(SearchProductCriteria criteria, PagingRequest pagingRequest);
}
