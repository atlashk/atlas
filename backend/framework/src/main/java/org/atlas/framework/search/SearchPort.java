package org.atlas.framework.search;

import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;
import org.atlas.framework.search.model.SearchCriteria;
import org.atlas.framework.search.model.SearchResponse;

public interface SearchPort {

  PagingResult<SearchResponse> search(SearchCriteria criteria, PagingRequest pagingRequest);
}
