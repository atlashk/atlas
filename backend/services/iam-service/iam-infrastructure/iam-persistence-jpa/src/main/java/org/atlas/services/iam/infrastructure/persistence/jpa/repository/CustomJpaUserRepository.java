package org.atlas.services.iam.infrastructure.persistence.jpa.repository;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.iam.infrastructure.persistence.jpa.entity.JpaUserEntity;
import org.atlas.services.iam.port.out.repository.UserRepository;

public interface CustomJpaUserRepository {

  List<JpaUserEntity> findByCriteria(UserRepository.FindUserCriteria criteria,
      PagingRequest pagingRequest);

  long countByCriteria(UserRepository.FindUserCriteria criteria);
}
