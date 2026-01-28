package org.atlas.user.persistence.jpa.repository;

import java.util.List;
import org.atlas.user.application.port.repository.criteria.FindUserCriteria;
import org.atlas.common.framework.paging.PagingRequest;
import org.atlas.user.persistence.jpa.entity.JpaUser;

public interface CustomJpaUserRepository {

  List<JpaUser> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindUserCriteria criteria);
}
