package org.atlas.infrastructure.persistence.jpa.adapter.user.repository;

import java.util.List;
import org.atlas.application.user.port.repository.criteria.FindUserCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.infrastructure.persistence.jpa.adapter.user.entity.JpaUser;

public interface CustomJpaUserRepository {

  List<JpaUser> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindUserCriteria criteria);
}
