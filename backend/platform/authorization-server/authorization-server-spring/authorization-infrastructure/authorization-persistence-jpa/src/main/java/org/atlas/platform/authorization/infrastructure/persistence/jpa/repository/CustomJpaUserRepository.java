package org.atlas.platform.authorization.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.platform.authorization.infrastructure.persistence.jpa.entity.JpaUserEntity;
import org.atlas.platform.authorization.port.out.repository.UserRepository;

public interface CustomJpaUserRepository {

  List<JpaUserEntity> findByCriteria(UserRepository.FindUserCriteria criteria,
      PagingRequest pagingRequest);

  long countByCriteria(UserRepository.FindUserCriteria criteria);
}
