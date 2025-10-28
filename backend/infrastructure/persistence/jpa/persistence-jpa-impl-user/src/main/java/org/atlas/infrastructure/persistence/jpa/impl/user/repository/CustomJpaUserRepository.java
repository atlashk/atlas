package org.atlas.infrastructure.persistence.jpa.impl.user.repository;

import java.util.List;
import org.atlas.domain.user.repository.criteria.FindUserCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.infrastructure.persistence.jpa.impl.user.entity.JpaUser;

public interface CustomJpaUserRepository {

  List<JpaUser> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindUserCriteria criteria);
}
