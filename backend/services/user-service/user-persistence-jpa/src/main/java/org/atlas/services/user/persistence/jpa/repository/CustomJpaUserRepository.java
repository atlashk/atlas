package org.atlas.services.user.persistence.jpa.repository;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.user.application.port.repository.criteria.FindUserCriteria;
import org.atlas.services.user.persistence.jpa.entity.JpaUser;

public interface CustomJpaUserRepository {

  List<JpaUser> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindUserCriteria criteria);
}
